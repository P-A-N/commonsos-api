package commonsos.service.multithread;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

import commonsos.Cache;
import commonsos.repository.entity.Community;
import commonsos.service.blockchain.BlockchainConsumer;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateCommunityTotalSupplyTask extends AbstractTask {

  @Inject BlockchainService blockchainService;
  @Inject BlockchainEventService blockchainEventService;
  @Inject Cache cache;

  private final Community community;
  private final BigDecimal absAmount;
  private final boolean isBurn;

  public UpdateCommunityTotalSupplyTask(Community community, BigDecimal absAmount, boolean isBurn) {
    this.community = community;
    this.absAmount = absAmount;
    this.isBurn = isBurn;
  }

  @Override
  public void runTask() {
    log.info(String.format("Update community totalSupply task start. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
    
    // update totalSupply on blockchain
    String hash = blockchainService.updateTotalSupply(community, absAmount, isBurn);
    
    BlockchainConsumer<Block> bc = new BlockchainConsumer<Block>() {
      int count = 0;
      int limit = 100;
      boolean isDone = false;
      @Override
      public void accept(Block block) throws Exception {
        List<String> transactions = block.getTransactions().stream().map(TransactionResult::get).map(Object::toString).collect(Collectors.toList());
        if (transactions.contains(hash)) {
          cache.removeTotalSupply(community.getTokenContractAddress());
          isDone = true;
        }
        count++;
        if (count >= limit) {
          log.warn(String.format("couldn't find the transactionHash in past %d blocks. It mait been failed at transaction.", limit));
          cache.removeTotalSupply(community.getTokenContractAddress());
          isDone = true;
        }
      }
      @Override
      public boolean isDone() {
        return isDone;
      }
    };
    
    blockchainEventService.addBlockConsumer(bc);
    cache.removeTotalSupply(community.getTokenContractAddress());
    
    log.info(String.format("Update community totalSupply task done. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
  }
  
  @Override
  public String toString() {
    return String.format("%s, communityId=%d", this.getClass().getSimpleName(), community.getId());
  }
}
