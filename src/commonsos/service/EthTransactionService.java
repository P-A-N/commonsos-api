package commonsos.service;

import static java.lang.String.format;
import static java.time.Instant.now;

import java.math.BigDecimal;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.crypto.Credentials;

import commonsos.command.admin.CreateEthTransactionCommand;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.EthTransactionRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.EthTransaction;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.EthBalance;
import commonsos.util.AdminUtil;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class EthTransactionService {

  @Inject private EthTransactionRepository repository;
  @Inject private CommunityRepository communityRepository;
  @Inject private BlockchainService blockchainService;
  @Inject private BlockchainEventService blockchainEventService;

  public EthBalance getEthBalance(Admin admin, Long communityId) {
    if (!AdminUtil.isSeeableCommunity(admin, communityId)) throw new ForbiddenException();
    Community community = communityRepository.findStrictById(communityId);
    EthBalance ethBalance = blockchainService.getEthBalance(community);
    return ethBalance;
  }
  
  public EthTransaction createEthTransaction(Admin admin, CreateEthTransactionCommand command) {
    if (!AdminUtil.isCreatableEthTransaction(admin)) throw new ForbiddenException();
    Community beneficiaryCommunity = communityRepository.findStrictById(command.getBeneficiaryCommunityId());
    
    BigDecimal amount = command.getAmount();
    BigDecimal balance = blockchainService.getSystemEthBalance();
    if (balance.compareTo(amount) < 0) throw new DisplayableException("error.notEnoughFunds");

    // create transaction
    EthTransaction transaction = new EthTransaction()
      .setCommunityId(command.getBeneficiaryCommunityId())
      .setRemitterAdminId(admin.getId())
      .setAmount(amount);
    repository.create(transaction);

    // send ether
    Credentials credentials = blockchainService.systemCredentials();
    String beneficiaryAddress = beneficiaryCommunity.getMainWalletAddress();
    String blockchainTransactionHash = blockchainService.transferEther(credentials, beneficiaryAddress, amount, false);

    repository.lockForUpdate(transaction);
    transaction.setBlockchainTransactionHash(blockchainTransactionHash);
    repository.update(transaction);

    blockchainEventService.checkTransaction(blockchainTransactionHash);

    return transaction;
  }

  public void markTransactionCompleted(String blockChainTransactionHash) {
    Optional<EthTransaction> result = repository.findByBlockchainTransactionHash(blockChainTransactionHash);
    if (!result.isPresent()) {
      return;
    }

    EthTransaction transaction = result.get();

    if (transaction.getBlockchainCompletedAt() != null) {
      log.info(format("EthTransaction %s already marked completed at %s", transaction.getBlockchainTransactionHash(), transaction.getBlockchainCompletedAt()));
      return;
    }

    repository.lockForUpdate(transaction);
    transaction.setBlockchainCompletedAt(now());
    repository.update(transaction);

    log.info(format("EthTransaction %s marked completed", transaction.getBlockchainTransactionHash()));
  }
}
