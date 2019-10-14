package commonsos.service.blockchain;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Transaction;

import commonsos.exception.ServerErrorException;
import commonsos.repository.EntityManagerService;
import commonsos.service.AbstractService;
import commonsos.service.EthTransactionService;
import commonsos.service.TokenTransactionService;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class BlockchainEventService extends AbstractService {

  @Inject private Web3j web3j;
  @Inject private TokenTransactionService tokenTransactionService;
  @Inject private EthTransactionService ethTransactionService;
  @Inject private EntityManagerService entityManagerService;

  public void listenEvents() {
    web3j.transactionFlowable().subscribe(tx -> {
      log.info(String.format("New transaction received: hash=%s, blockNumber=%s, from=%s, to=%s, gas=%d, gasPrice=%d", tx.getHash(), tx.getBlockNumber(), tx.getFrom(), tx.getTo(), tx.getGas(), tx.getGasPrice()));
    });
    
    web3j.pendingTransactionFlowable().subscribe(tx -> {
      log.info(String.format("New Pending transaction received: hash=%s, from=%s, to=%s, gas=%d, gasPrice=%d",
          tx.getHash(), tx.getFrom(), tx.getTo(), tx.getGas(), tx.getGasPrice()));
    });
    
    web3j.blockFlowable(false).subscribe(ethBlock -> {
      Block block = ethBlock.getResult();
      List<String> transactions = block.getTransactions().stream().map(TransactionResult::get).map(Object::toString).collect(Collectors.toList());
      log.info(String.format("New Block received: number=%d, hash=%s, timestamp=%d gasLimit=%d, gasUsed=%d, parentHash=%s, transactionCount=%s transactions=%s",
          block.getNumber(), block.getHash(), block.getTimestamp(), block.getGasLimit(), block.getGasUsed(), block.getParentHash(), transactions.size(), ArrayUtils.toString(transactions)));
      
      for (String transaction : transactions) {
        markTransactionCompleted(transaction);
      }
    });
  }
  
  public void checkTransaction(String transactionHash) {
    try {
      Transaction transaction = web3j.ethGetTransactionByHash(transactionHash).send().getResult();
      String blockHash = transaction.getBlockHash();
      if (StringUtils.isNotEmpty(blockHash) && !"0x0000000000000000000000000000000000000000000000000000000000000000".equals(blockHash)) {
        tokenTransactionService.markTransactionCompleted(transactionHash);
      }
    } catch (IOException e) {
      throw new ServerErrorException();
    }
  }
  
  private void markTransactionCompleted(String transactionHash) {
    entityManagerService.runInTransaction(() -> {
      tokenTransactionService.markTransactionCompleted(transactionHash);
      ethTransactionService.markTransactionCompleted(transactionHash);
      return Void.class;
    });
  }
}
