package commonsos.util;

import commonsos.repository.entity.WalletType;
import commonsos.service.blockchain.TokenBalance;
import commonsos.view.CommunityTokenBalanceView;
import commonsos.view.UserTokenBalanceView;

public class TransactionUtil {
  
  private TransactionUtil() {}

  public static UserTokenBalanceView userTokenBalanceView(TokenBalance tokenBalance) {
    return new UserTokenBalanceView()
        .setCommunityId(tokenBalance.getCommunityId())
        .setTokenSymbol(tokenBalance.getToken().getTokenSymbol())
        .setBalance(tokenBalance.getBalance());
  }

  public static CommunityTokenBalanceView communityTokenBalanceView(TokenBalance tokenBalance, WalletType walletType) {
    return new CommunityTokenBalanceView()
        .setCommunityId(tokenBalance.getCommunityId())
        .setWallet(walletType)
        .setTokenSymbol(tokenBalance.getToken().getTokenSymbol())
        .setBalance(tokenBalance.getBalance());
  }
}
