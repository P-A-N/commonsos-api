package commonsos.controller.app.transaction;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.TokenTransactionService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.app.TransactionListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class TransactionListController extends AfterAppLoginController {

  @Inject private TokenTransactionService service;

  @Override
  public TransactionListView handleAfterLogin(User user, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    TransactionListView view = service.transactions(user, communityId, paginationCommand);
    return view;
  }
}
