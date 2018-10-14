package commonsos.controller.auth;

import commonsos.repository.user.User;
import commonsos.service.blockchain.BlockchainService;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

import static commonsos.service.blockchain.BlockchainService.GAS_PRICE;
import static commonsos.service.blockchain.BlockchainService.TOKEN_TRANSFER_GAS_LIMIT;

import java.math.BigInteger;

@Slf4j
@EqualsAndHashCode @ToString
public class DelegateWalletTask implements Runnable {

  @Inject BlockchainService blockchainService;

  private final User walletOwner;
  private final User delegate;

  public DelegateWalletTask(User walletOwner, User delegate) {
    this.walletOwner = walletOwner;
    this.delegate = delegate;
  }

  @Override public void run() {
    log.info(String.format("Delegating user %s %s wallet to %s", walletOwner.getUsername(), walletOwner.getWalletAddress(), delegate.getWalletAddress()));
    blockchainService.transferEther(delegate, walletOwner.getWalletAddress(), TOKEN_TRANSFER_GAS_LIMIT.multiply(BigInteger.TEN).multiply(GAS_PRICE));
    blockchainService.delegateTokenTransferRight(walletOwner, delegate);
  }
}
