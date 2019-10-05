package commonsos.controller.app.transaction;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.TokenTransactionService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.TokenTransactionListView;
import spark.Request;
import spark.Response;

public class TransactionListController extends AfterAppLoginController {

  @Inject private TokenTransactionService service;

  @Override
  public TokenTransactionListView handleAfterLogin(User user, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    TokenTransactionListView view = service.searchUserTranByUser(user, communityId, paginationCommand);
    return view;
  }
}
