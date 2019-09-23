package commonsos.controller.app.transaction;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.TokenTransactionService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.TransactionListView;
import spark.Request;
import spark.Response;

public class AdminTransactionListController extends AfterAppLoginController {

  @Inject private TokenTransactionService service;

  @Override
  public TransactionListView handleAfterLogin(User user, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    Long userId = RequestUtil.getQueryParamLong(request, "userId", true);
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    TransactionListView view = service.transactionsForAdminUser(user, communityId, userId, paginationCommand);
    return view;
  }
}