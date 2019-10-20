package commonsos.controller.admin.user;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.service.TokenTransactionService;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.TokenTransactionListView;
import spark.Request;
import spark.Response;

public class SearchUserTransactionsByAdminController extends AfterAdminLoginController {

  @Inject TokenTransactionService tokenTransactionService;
  
  @Override
  protected TokenTransactionListView handleAfterLogin(Admin admin, Request request, Response response) {
    Long userId = RequestUtil.getPathParamLong(request, "id");
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);

    if (!AdminUtil.isSeeableCommunity(admin, communityId)) throw new ForbiddenException();
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    TokenTransactionListView view = tokenTransactionService.searchUserTranByAdmin(userId, communityId, paginationCommand);
    return view;
  }
}
