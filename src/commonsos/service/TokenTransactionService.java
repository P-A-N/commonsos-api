package commonsos.service;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;
import static spark.utils.StringUtils.isBlank;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.command.PaginationCommand;
import commonsos.service.command.TransactionCreateCommand;
import commonsos.service.notification.PushNotificationService;
import commonsos.util.PaginationUtil;
import commonsos.util.UserUtil;
import commonsos.view.app.BalanceView;
import commonsos.view.app.TransactionListView;
import commonsos.view.app.TransactionView;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class TokenTransactionService {
  
  @Inject TokenTransactionRepository repository;
  @Inject UserRepository userRepository;
  @Inject AdRepository adRepository;
  @Inject CommunityRepository communityRepository;
  @Inject BlockchainService blockchainService;
  @Inject BlockchainEventService blockchainEventService;
  @Inject PushNotificationService pushNotificationService;

  public BalanceView balance(User user, Long communityId) {
    communityRepository.findPublicStrictById(communityId);
    BigDecimal tokenBalance = blockchainService.tokenBalance(user, communityId);
    BalanceView view = new BalanceView()
        .setCommunityId(communityId)
        .setTokenSymbol(blockchainService.tokenSymbol(communityId))
        .setBalance(tokenBalance.subtract(repository.pendingTransactionsAmount(user.getId(), communityId)));
    return view;
  }
  
  private boolean isDebit(User user, TokenTransaction transaction) {
    return transaction.getRemitterId().equals(user.getId());
  }

  public TransactionListView transactionsByAdmin(User admin, Long communityId, Long userId, PaginationCommand pagination) {
    if (!UserUtil.isAdmin(admin, communityId)) throw new ForbiddenException(String.format("user is not a admin.[userId=%d, communityId=%d]", admin.getId(), communityId));
    
    User user = userRepository.findStrictById(userId);
    return transactions(user, communityId, pagination);
  }

  public TransactionListView transactions(User user, Long communityId, PaginationCommand pagination) {
    communityRepository.findPublicStrictById(communityId);
    ResultList<TokenTransaction> result = repository.transactions(user, communityId, null);

    List<TransactionView> transactionViews = result.getList().stream()
        .sorted(Comparator.comparing(TokenTransaction::getCreatedAt).reversed())
        .map(transaction -> view(user, transaction))
        .collect(toList());
    
    TransactionListView listView = new TransactionListView();
    listView.setPagination(PaginationUtil.toView(transactionViews, pagination));
    List<TransactionView> paginationedViews = PaginationUtil.pagination(transactionViews, pagination);
    listView.setTransactionList(paginationedViews);
    
    return listView;
  }

  public TransactionView view(User user, TokenTransaction transaction) {
    TransactionView view = new TransactionView()
      .setAmount(transaction.getAmount())
      .setDescription(transaction.getDescription())
      .setCreatedAt(transaction.getCreatedAt())
      .setCompleted(transaction.getBlockchainCompletedAt() != null)
      .setDebit(isDebit(user, transaction));

    Optional<User> remitter = userRepository.findById(transaction.getRemitterId());
    Optional<User> beneficiary = userRepository.findById(transaction.getBeneficiaryId());
    
    if (remitter.isPresent()) view.setRemitter(UserUtil.publicView(remitter.get()));
    if (beneficiary.isPresent()) view.setBeneficiary(UserUtil.publicView(beneficiary.get()));
    
    return view;
  }

  public TokenTransaction create(User user, TransactionCreateCommand command) {
    if (command.getCommunityId() == null) throw new BadRequestException("communityId is required");
    if (isBlank(command.getDescription()))  throw new BadRequestException("description is blank");
    if (ZERO.compareTo(command.getAmount()) > -1)  throw new BadRequestException("sending negative point");
    if (user.getId().equals(command.getBeneficiaryId())) throw new BadRequestException("user is beneficiary");
    User beneficiary = userRepository.findStrictById(command.getBeneficiaryId());
    
    Community community = communityRepository.findPublicStrictById(command.getCommunityId());
    if (!UserUtil.isMember(user, community)) throw new DisplayableException("error.userIsNotCommunityMember");
    if (!UserUtil.isMember(beneficiary, community)) throw new DisplayableException("error.beneficiaryIsNotCommunityMember");

    if (command.getAdId() != null) {
      Ad ad = adRepository.findStrict(command.getAdId());
      if (!ad.getCommunityId().equals(community.getId())) throw new BadRequestException("communityId does not match with ad");
    }
    BalanceView balanceView = balance(user, command.getCommunityId());
    if (balanceView.getBalance().compareTo(command.getAmount()) < 0) throw new DisplayableException("error.notEnoughFunds");

    TokenTransaction transaction = new TokenTransaction()
      .setCommunityId(command.getCommunityId())
      .setRemitterId(user.getId())
      .setAmount(command.getAmount())
      .setBeneficiaryId(command.getBeneficiaryId())
      .setDescription(command.getDescription())
      .setAdId(command.getAdId());

    repository.create(transaction);

    String blockchainTransactionHash = blockchainService.transferTokens(user, beneficiary, command.getCommunityId(), transaction.getAmount());
    transaction.setBlockchainTransactionHash(blockchainTransactionHash);

    repository.update(transaction);
    blockchainEventService.checkTransaction(blockchainTransactionHash);

    return transaction;
  }

  public void markTransactionCompleted(String blockChainTransactionHash) {
    Optional<TokenTransaction> result = repository.findByBlockchainTransactionHash(blockChainTransactionHash);
    if (!result.isPresent()) {
      return;
    }

    TokenTransaction transaction = result.get();

    if (transaction.getBlockchainCompletedAt() != null) {
      log.info(format("Transaction %s already marked completed at %s", transaction.getBlockchainTransactionHash(), transaction.getBlockchainCompletedAt()));
      return;
    }

    transaction.setBlockchainCompletedAt(now());
    repository.update(transaction);

    User beneficiary = userRepository.findStrictById(transaction.getBeneficiaryId());
    User remitter = userRepository.findStrictById(transaction.getRemitterId());
    pushNotificationService.send(beneficiary, format("%s\n+%.2f %s", remitter.getUsername(), transaction.getAmount(), transaction.getDescription()));

    log.info(format("Transaction %s marked completed", transaction.getBlockchainTransactionHash()));
  }
}
