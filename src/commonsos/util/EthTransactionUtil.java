package commonsos.util;

import commonsos.service.blockchain.EthBalance;
import commonsos.view.EthBalanceView;

public class EthTransactionUtil {
  
  private EthTransactionUtil() {}

  public static EthBalanceView ethBalanceView(EthBalance ethBalance) {
    return new EthBalanceView()
        .setBalance(ethBalance.getBalance());
  }
}
