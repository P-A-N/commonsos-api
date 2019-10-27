package commonsos.runnable;

import static commonsos.service.blockchain.BlockchainService.GAS_PRICE;
import static commonsos.service.blockchain.BlockchainService.INITIAL_TOKEN_AMOUNT;
import static commonsos.service.blockchain.BlockchainService.TOKEN_TRANSFER_GAS_LIMIT;

import java.math.BigInteger;

import javax.inject.Inject;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelegateWalletTask implements Runnable {

  @Inject BlockchainService blockchainService;

  private final User walletOwner;
  private final Community community;

  public DelegateWalletTask(User walletOwner, Community community) {
    this.walletOwner = walletOwner;
    this.community = community;
  }

  @Override
  public void run() {
    if (blockchainService.isAllowed(walletOwner, community, INITIAL_TOKEN_AMOUNT.divide(BigInteger.TEN))) return;
    
    log.info(String.format("Delegating user. [userId=%d, communityId=%d]", walletOwner.getId(), community.getId()));
    blockchainService.transferEther(community, walletOwner.getWalletAddress(), TOKEN_TRANSFER_GAS_LIMIT.multiply(BigInteger.TEN).multiply(GAS_PRICE), true);
    blockchainService.approveFromUser(walletOwner, community);
  }
}
