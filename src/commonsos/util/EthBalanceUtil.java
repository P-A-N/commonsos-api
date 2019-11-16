package commonsos.util;

import commonsos.repository.entity.EthBalanceHistory;
import commonsos.view.EthBalanceView;

public class EthBalanceUtil {
  
  private EthBalanceUtil() {}
  
  public static EthBalanceView toHistoryView(EthBalanceHistory history) {
    return new EthBalanceView()
        .setBaseDate(history.getBaseDate())
        .setBalance(history.getEthBalance());
  }
}
