package commonsos.service.blockchain;

import static commonsos.service.blockchain.BlockchainService.GAS_PRICE;
import static commonsos.service.blockchain.BlockchainService.INITIAL_TOKEN_AMOUNT;
import static commonsos.service.blockchain.BlockchainService.TOKEN_TRANSFER_GAS_LIMIT;

import java.math.BigInteger;

import javax.inject.Inject;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode @ToString
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
    
    log.info(String.format("Delegating user %s %s wallet to %s", walletOwner.getUsername(), walletOwner.getWalletAddress(), community.getAdminUser().getWalletAddress()));
    blockchainService.transferEther(community.getAdminUser(), walletOwner.getWalletAddress(), TOKEN_TRANSFER_GAS_LIMIT.multiply(BigInteger.TEN).multiply(GAS_PRICE));
    blockchainService.delegateTokenTransferRight(walletOwner, community);
  }
}
