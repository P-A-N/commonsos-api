package commonsos.controller.batch.community;

import static commonsos.annotation.SyncObject.ETH_BALANCE_HISTORY;

import javax.inject.Inject;

import commonsos.annotation.Synchronized;
import commonsos.controller.batch.AbstractBatchController;
import commonsos.service.EthBalanceHistoryService;
import spark.Request;
import spark.Response;

@Synchronized(ETH_BALANCE_HISTORY)
public class CreateEthBalanceHistoryBatchController extends AbstractBatchController {

  @Inject private EthBalanceHistoryService ethBalanceHistoryService;
  
  @Override
  protected void handleBatch(Request request, Response response) {
    ethBalanceHistoryService.createEthBalanceHistory();
  }
}
