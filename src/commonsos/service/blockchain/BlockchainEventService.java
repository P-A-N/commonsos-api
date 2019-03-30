package commonsos.service.blockchain;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import commonsos.ThreadValue;
import commonsos.exception.ServerErrorException;
import commonsos.repository.EntityManagerService;
import commonsos.service.TransactionService;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class BlockchainEventService {

  @Inject private Web3j web3j;
  @Inject private TransactionService transactionService;
  @Inject private EntityManagerService entityManagerService;

  public void listenEvents() {
    web3j.transactionFlowable().subscribe(tx -> {
      log.info(String.format("New transaction received: hash=%s, from=%s, to=%s, gas=%d ", tx.getHash(), tx.getFrom(), tx.getTo(), tx.getGas()));
      entityManagerService.runInTransaction(() -> {
        ThreadValue.setReadOnly(false);
        transactionService.markTransactionCompleted(tx.getHash());
        return Void.class;
      });
    });
    
    web3j.pendingTransactionFlowable().subscribe(tx -> {
      log.info(String.format("Pending transaction received: hash=%s, from=%s, to=%s, gas=%d ", tx.getHash(), tx.getFrom(), tx.getTo(), tx.getGas()));
    });
  }
  
  public void checkTransaction(String transactionHash) {
    try {
      TransactionReceipt receipt = web3j.ethGetTransactionReceipt(transactionHash).send().getResult();
      if (receipt != null && receipt.getBlockHash() != null) {
        transactionService.markTransactionCompleted(transactionHash);
      }
    } catch (IOException e) {
      throw new ServerErrorException();
    }
  }
}
