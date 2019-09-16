package commonsos.controller.admin.user;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.controller.command.PaginationCommand;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.service.TokenTransactionService;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.admin.TransactionListForAdminView;
import spark.Request;
import spark.Response;

public class SearchUserTransactionsController extends AfterAdminLoginController {

  @Inject TokenTransactionService tokenTransactionService;
  
  @Override
  protected TransactionListForAdminView handleAfterLogin(Admin admin, Request request, Response response) {
    Long userId = RequestUtil.getPathParamLong(request, "id");
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);

    if (!AdminUtil.isSeeable(admin, communityId)) throw new ForbiddenException();
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    TransactionListForAdminView view = tokenTransactionService.transactionsForAdmin(userId, communityId, paginationCommand);
    return view;
  }
}
