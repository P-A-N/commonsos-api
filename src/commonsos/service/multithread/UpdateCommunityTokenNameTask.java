package commonsos.service.multithread;

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
public class UpdateCommunityTokenNameTask extends AbstractTask {

  @Inject BlockchainService blockchainService;
  @Inject BlockchainEventService blockchainEventService;
  @Inject Cache cache;

  private final Community community;
  private final String newTokenName;

  public UpdateCommunityTokenNameTask(Community community, String newTokenName) {
    this.community = community;
    this.newTokenName = newTokenName;
  }

  @Override
  public void runTask() {
    log.info(String.format("Update community tokenName task start. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
    
    // update tokenName on blockchain
    String hash = blockchainService.updateTokenName(community, newTokenName);
    
    BlockchainConsumer<Block> bc = new BlockchainConsumer<Block>() {
      int count = 0;
      int limit = 100;
      boolean isDone = false;
      @Override
      public void accept(Block block) throws Exception {
        List<String> transactions = block.getTransactions().stream().map(TransactionResult::get).map(Object::toString).collect(Collectors.toList());
        if (transactions.contains(hash)) {
          cache.removeTokenName(community.getTokenContractAddress());
          isDone = true;
        }
        count++;
        if (count >= limit) {
          log.warn(String.format("couldn't find the transactionHash in past %d blocks. It mait been failed at transaction.", limit));
          cache.removeTokenName(community.getTokenContractAddress());
          isDone = true;
        }
      }
      @Override
      public boolean isDone() {
        return isDone;
      }
    };
    
    blockchainEventService.addBlockConsumer(bc);
    cache.removeTokenName(community.getTokenContractAddress());
    
    log.info(String.format("Update community tokenName task done. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
  }
  
  @Override
  public String toString() {
    return String.format("%s, communityId=%d", this.getClass().getSimpleName(), community.getId());
  }
}
