package commonsos.controller.batch.community.redistribution;

import static commonsos.annotation.SyncObject.REDISTRIBUTION;

import javax.inject.Inject;

import commonsos.annotation.Synchronized;
import commonsos.command.batch.RedistributionBatchCommand;
import commonsos.controller.batch.AbstractBatchController;
import commonsos.service.RedistributionService;
import commonsos.service.TokenTransactionService;
import spark.Request;
import spark.Response;

@Synchronized(REDISTRIBUTION)
public class RedistributionBatchController extends AbstractBatchController {

  @Inject private RedistributionService redistributionService;
  @Inject private TokenTransactionService tokenTransactionService;
  
  @Override
  protected void handleBatch(Request request, Response response) {
    RedistributionBatchCommand command = redistributionService.createRedistributionCommand();
    tokenTransactionService.create(command);
  }
}
