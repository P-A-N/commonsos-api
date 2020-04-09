package commonsos.service.multithread;

import javax.inject.Inject;

import org.web3j.crypto.Credentials;

import commonsos.repository.EthTransactionRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.EthTransaction;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;

public class CreateEthTransactionTask extends AbstractTask {

  @Inject private BlockchainService blockchainService;
  @Inject private BlockchainEventService blockchainEventService;
  @Inject private EthTransactionRepository ethTransactionrepository;

  private final Community beneficiaryCommunity;
  private final Long transactionId;

  public CreateEthTransactionTask(
      Community beneficiaryCommunity,
      EthTransaction transaction) {
    this.beneficiaryCommunity = beneficiaryCommunity;
    this.transactionId = transaction.getId();
  }
  
  @Override
  protected int getMaxRepeatCount() {return 1;}

  @Override
  public void runTask() {
    // send ether
    EthTransaction transaction = ethTransactionrepository.findStrictById(transactionId);
    Credentials credentials = blockchainService.systemCredentials();
    String beneficiaryAddress = beneficiaryCommunity.getMainWalletAddress();
    String blockchainTransactionHash = blockchainService.transferEther(credentials, beneficiaryAddress, transaction.getAmount(), false);

    ethTransactionrepository.lockForUpdate(transaction);
    transaction.setBlockchainTransactionHash(blockchainTransactionHash);
    ethTransactionrepository.update(transaction);

    blockchainEventService.checkTransaction(blockchainTransactionHash);
  }
  
  @Override
  public String toString() {
    return String.format("%s, beneficiaryCommunityId=%d, transactionId=%d", this.getClass().getSimpleName(), beneficiaryCommunity.getId(), transactionId);
  }
}
