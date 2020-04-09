package commonsos.service.multithread;

import static commonsos.service.blockchain.BlockchainService.INITIAL_TOKEN_AMOUNT;
import static commonsos.service.blockchain.BlockchainService.TOKEN_TRANSFER_GAS_LIMIT;

import java.math.BigInteger;

import javax.inject.Inject;

import commonsos.Configuration;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApproveAdministratorTask extends AbstractTask {

  @Inject BlockchainService blockchainService;
  @Inject Configuration config;

  private final User walletOwner;
  private final Community community;

  public ApproveAdministratorTask(User walletOwner, Community community) {
    this.walletOwner = walletOwner;
    this.community = community;
  }

  @Override
  public void runTask() {
    if (blockchainService.isAllowed(walletOwner, community, INITIAL_TOKEN_AMOUNT.divide(BigInteger.TEN))) return;
    
    log.info(String.format("Delegating user. [userId=%d, communityId=%d]", walletOwner.getId(), community.getId()));
    blockchainService.transferEther(community, walletOwner.getWalletAddress(), TOKEN_TRANSFER_GAS_LIMIT.multiply(BigInteger.TEN).multiply(config.gasPrice()), true);
    blockchainService.approveFromUser(walletOwner, community);
  }
  
  @Override
  public String toString() {
    Long userId = walletOwner == null ? null : walletOwner.getId();
    Long communityId = community == null ? null : community.getId();
    return String.format("%s, userId=%d, communityId=%d", this.getClass().getSimpleName(), userId, communityId);
  }
}
