package commonsos.controller.admin.user;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.command.admin.SearchUserForAdminCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.service.UserService;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.UserListView;
import spark.Request;
import spark.Response;

public class SearchUsersByAdminController extends AfterAdminLoginController {

  @Inject UserService userService;
  
  @Override
  protected UserListView handleAfterLogin(Admin admin, Request request, Response response) {
    SearchUserForAdminCommand command = new SearchUserForAdminCommand()
        .setUsername(RequestUtil.getQueryParamString(request, "username", false))
        .setEmailAddress(RequestUtil.getQueryParamString(request, "emailAddress", false))
        .setCommunityId(RequestUtil.getQueryParamLong(request, "communityId", false))
        .setOmiteBalance(RequestUtil.getQueryParamBoolean(request, "isOmiteBalance", false));

    if (!AdminUtil.isSeeableCommunity(admin, command.getCommunityId())) throw new ForbiddenException();
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    UserListView view = userService.searchUsersForAdmin(admin, command, paginationCommand);
    return view;
  }
}
