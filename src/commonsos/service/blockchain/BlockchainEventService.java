package commonsos.service.blockchain;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Transaction;

import commonsos.ThreadValue;
import commonsos.repository.EntityManagerService;
import commonsos.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import rx.functions.Action1;

@Singleton
@Slf4j
public class BlockchainEventService {

  @Inject private Web3j web3j;
  @Inject private TransactionService transactionService;
  @Inject private EntityManagerService entityManagerService;

  public void listenEvents() {
    Action1<Transaction> onNext = tx -> {
      log.info(String.format("New transaction event received: hash=%s, from=%s, to=%s, gas=%d ", tx.getHash(), tx.getFrom(), tx.getTo(), tx.getGas()));

      entityManagerService.runInTransaction(() -> {
        ThreadValue.setReadOnly(false);
        transactionService.markTransactionCompleted(tx.getHash());
        return Void.class;
      });
    };
    
    web3j.catchUpToLatestTransactionObservable(DefaultBlockParameterName.EARLIEST).subscribe(onNext);
    web3j.transactionObservable().subscribe(onNext);
  }
}
