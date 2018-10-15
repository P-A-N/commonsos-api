package commonsos.controller.auth;

import static org.mockito.Mockito.mock;

import commonsos.service.blockchain.BlockchainService;

public class DelegateWalletTaskTest {

  BlockchainService blockchainService = mock(BlockchainService.class);

  // TODO
  /*@Test
  public void run() {
    User delegate = new User().setWalletAddress("community wallet");
    User walletOwner = new User().setWalletAddress("member wallet");
    DelegateWalletTask task = new DelegateWalletTask(walletOwner, delegate);
    task.blockchainService = blockchainService;

    task.run();

    verify(blockchainService).transferEther(delegate, "member wallet", new BigInteger("16200000000000000"));
    verify(blockchainService).delegateTokenTransferRight(walletOwner, delegate);
  }*/
}