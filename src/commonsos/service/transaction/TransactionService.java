package commonsos.service.transaction;

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

import commonsos.DisplayableException;
import commonsos.exception.BadRequestException;
import commonsos.exception.UserNotFoundException;
import commonsos.repository.ad.Ad;
import commonsos.repository.transaction.Transaction;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.PushNotificationService;
import commonsos.service.ad.AdService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.util.UserUtil;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class TransactionService {
  @Inject TransactionRepository repository;
  @Inject UserRepository userRepository;
  @Inject UserUtil userUtil;
  @Inject BlockchainService blockchainService;
  @Inject AdService adService;
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
    User remitter = userRepository.findById(transaction.getRemitterId()).orElseThrow(UserNotFoundException::new);
    User beneficiary = userRepository.findById(transaction.getBeneficiaryId()).orElseThrow(UserNotFoundException::new);
    return new TransactionView()
      .setRemitter(userUtil.view(remitter))
      .setBeneficiary(userUtil.view(beneficiary))
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
    User beneficiary = userRepository.findById(command.getBeneficiaryId()).orElseThrow(UserNotFoundException::new);

    if (command.getAdId() != null) {
      Ad ad = adService.ad(command.getAdId());
      if (!adService.isPayableByUser(user, ad)) throw new BadRequestException();
      if (!beneficiary.getJoinedCommunities().stream().anyMatch(c -> c.getId().equals(command.getCommunityId()))) throw new BadRequestException();
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

    User beneficiary = userRepository.findById(transaction.getBeneficiaryId()).orElseThrow(UserNotFoundException::new);
    User remitter = userRepository.findById(transaction.getRemitterId()).orElseThrow(UserNotFoundException::new);
    pushNotificationService.send(beneficiary, format("%s\n+%.2f %s", remitter.getUsername(), transaction.getAmount(), transaction.getDescription()));

    log.info(format("Transaction %s marked completed", transaction.getBlockchainTransactionHash()));
  }
}
