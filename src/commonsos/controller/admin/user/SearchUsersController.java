package commonsos.controller.admin.user;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.controller.command.PaginationCommand;
import commonsos.controller.command.admin.SearchUserForAdminCommand;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.service.UserService;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.admin.UserListForAdminView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchUsersController extends AfterAdminLoginController {

  @Inject UserService userService;
  
  @Override
  protected UserListForAdminView handleAfterLogin(Admin admin, Request request, Response response) {
    SearchUserForAdminCommand command = new SearchUserForAdminCommand()
        .setUsername(RequestUtil.getQueryParamString(request, "username", false))
        .setEmailAddress(RequestUtil.getQueryParamString(request, "emailAddress", false))
        .setCommunityId(RequestUtil.getQueryParamLong(request, "communityId", false));

    if (!AdminUtil.isSeeable(admin, command.getCommunityId())) throw new ForbiddenException();
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    UserListForAdminView view = userService.searchUsersForAdmin(admin, command, paginationCommand);
    return view;
  }
}
