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

import commonsos.BadRequestException;
import commonsos.DisplayableException;
import commonsos.repository.ad.Ad;
import commonsos.repository.transaction.Transaction;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.service.PushNotificationService;
import commonsos.service.ad.AdService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.user.UserService;
import commonsos.service.view.UserView;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class TransactionService {
  @Inject TransactionRepository repository;
  @Inject BlockchainService blockchainService;
  @Inject UserService userService;
  @Inject AdService adService;
  @Inject PushNotificationService pushNotificationService;

  public BigDecimal balance(User user) {
    BigDecimal tokenBalance = blockchainService.tokenBalance(user);
    return tokenBalance.subtract(repository.pendingTransactionsAmount(user.getId()));
  }

  private boolean isDebit(User user, Transaction transaction) {
    return transaction.getRemitterId().equals(user.getId());
  }

  public List<TransactionView> transactions(User user) {
    return repository.transactions(user).stream()
      .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
      .map(transaction -> view(user, transaction))
      .collect(toList());
  }

  public TransactionView view(User user, Transaction transaction) {
    UserView remitter = userService.view(transaction.getRemitterId());
    UserView beneficiary = userService.view(transaction.getBeneficiaryId());
    return new TransactionView()
      .setRemitter(remitter)
      .setBeneficiary(beneficiary)
      .setAmount(transaction.getAmount())
      .setDescription(transaction.getDescription())
      .setCreatedAt(transaction.getCreatedAt())
      .setCompleted(transaction.getBlockchainCompletedAt() != null)
      .setDebit(isDebit(user, transaction));
  }

  public Transaction create(User user, TransactionCreateCommand command) {
    if (isBlank(command.getDescription()))  throw new BadRequestException();
    if (ZERO.compareTo(command.getAmount()) > -1)  throw new BadRequestException();
    if (user.getId().equals(command.getBeneficiaryId())) throw new BadRequestException();
    User beneficiary = userService.user(command.getBeneficiaryId());

    if (command.getAdId() != null) {
      Ad ad = adService.ad(command.getAdId());
      if (!adService.isPayableByUser(user, ad)) throw new BadRequestException();
      if (!user.getCommunityId().equals(beneficiary.getCommunityId())) throw new BadRequestException();
    }
    BigDecimal balance = balance(user);
    if (balance.compareTo(command.getAmount()) < 0) throw new DisplayableException("error.notEnoughFunds");

    Transaction transaction = new Transaction()
      .setRemitterId(user.getId())
      .setAmount(command.getAmount())
      .setBeneficiaryId(command.getBeneficiaryId())
      .setDescription(command.getDescription())
      .setAdId(command.getAdId())
      .setCreatedAt(now());

    repository.create(transaction);

    String blockchainTransactionId = blockchainService.transferTokens(user, beneficiary, transaction.getAmount());
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

    User beneficiary = userService.user(transaction.getBeneficiaryId());
    String remitterName = userService.fullName(userService.user(transaction.getRemitterId()));
    pushNotificationService.send(beneficiary, format("%s\n+%.2f %s", remitterName, transaction.getAmount(), transaction.getDescription()));

    log.info(format("Transaction %s marked completed", transaction.getBlockchainTransactionHash()));
  }
}
