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
import commonsos.repository.AdRepository;
import commonsos.repository.TransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Transaction;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.command.TransactionCreateCommand;
import commonsos.service.notification.PushNotificationService;
import commonsos.util.AdUtil;
import commonsos.util.UserUtil;
import commonsos.view.BalanceView;
import commonsos.view.TransactionView;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class TransactionService {
  @Inject TransactionRepository repository;
  @Inject UserRepository userRepository;
  @Inject AdRepository adRepository;
  @Inject BlockchainService blockchainService;
  @Inject PushNotificationService pushNotificationService;

  public BalanceView balance(User user, Long communityId) {
    BigDecimal tokenBalance = blockchainService.tokenBalance(user, communityId);
    BalanceView view = new BalanceView()
        .setCommunityId(communityId)
        .setBalance(tokenBalance.subtract(repository.pendingTransactionsAmount(user.getId(), communityId)));
    return view;
  }
  
  private boolean isDebit(User user, Transaction transaction) {
    return transaction.getRemitterId().equals(user.getId());
  }

  public List<TransactionView> transactions(User user, Long communityId) {
    return repository.transactions(user, communityId).stream()
      .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
      .map(transaction -> view(user, transaction))
      .collect(toList());
  }

  public TransactionView view(User user, Transaction transaction) {
    User remitter = userRepository.findStrictById(transaction.getRemitterId());
    User beneficiary = userRepository.findStrictById(transaction.getBeneficiaryId());
    return new TransactionView()
      .setRemitter(UserUtil.view(remitter))
      .setBeneficiary(UserUtil.view(beneficiary))
      .setAmount(transaction.getAmount())
      .setDescription(transaction.getDescription())
      .setCreatedAt(transaction.getCreatedAt())
      .setCompleted(transaction.getBlockchainCompletedAt() != null)
      .setDebit(isDebit(user, transaction));
  }

  public Transaction create(User user, TransactionCreateCommand command) {
    if (command.getCommunityId() == null) throw new BadRequestException("communityId is required");
    if (isBlank(command.getDescription()))  throw new BadRequestException();
    if (ZERO.compareTo(command.getAmount()) > -1)  throw new BadRequestException();
    if (user.getId().equals(command.getBeneficiaryId())) throw new BadRequestException();
    User beneficiary = userRepository.findStrictById(command.getBeneficiaryId());

    if (command.getAdId() != null) {
      Ad ad = adRepository.findStrict(command.getAdId());
      if (!AdUtil.isPayableByUser(user, ad)) throw new BadRequestException();
      if (!beneficiary.getCommunityList().stream().anyMatch(c -> c.getId().equals(command.getCommunityId()))) throw new BadRequestException();
    }
    BalanceView balanceView = balance(user, command.getCommunityId());
    if (balanceView.getBalance().compareTo(command.getAmount()) < 0) throw new DisplayableException("error.notEnoughFunds");

    Transaction transaction = new Transaction()
      .setCommunityId(command.getCommunityId())
      .setRemitterId(user.getId())
      .setAmount(command.getAmount())
      .setBeneficiaryId(command.getBeneficiaryId())
      .setDescription(command.getDescription())
      .setAdId(command.getAdId())
      .setCreatedAt(now());

    repository.create(transaction);

    String blockchainTransactionId = blockchainService.transferTokens(user, beneficiary, command.getCommunityId(), transaction.getAmount());
    transaction.setBlockchainTransactionHash(blockchainTransactionId);

    repository.update(transaction);

    return transaction;
  }

  public void markTransactionCompleted(String blockChainTransactionHash) {
    Optional<Transaction> result = repository.findByBlockchainTransactionHash(blockChainTransactionHash);
    if (!result.isPresent()) {
      log.warn(format("Cannot mark transaction completed, hash %s not found", blockChainTransactionHash));
      return;
    }

    Transaction transaction = result.get();

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
