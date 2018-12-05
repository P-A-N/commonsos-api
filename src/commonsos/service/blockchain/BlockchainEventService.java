package commonsos.service.blockchain;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import commonsos.ThreadValue;
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
    web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(DefaultBlockParameterName.EARLIEST).subscribe(tx -> {
      log.info(String.format("New transaction event received: hash=%s, from=%s, to=%s, gas=%d ", tx.getHash(), tx.getFrom(), tx.getTo(), tx.getGas()));

      entityManagerService.runInTransaction(() -> {
        ThreadValue.setReadOnly(false);
        transactionService.markTransactionCompleted(tx.getHash());
        return Void.class;
      });
    });
  }
}
