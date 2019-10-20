package commonsos.controller.admin.user;

import java.util.List;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.admin.UpdateUserCommunitiesByAdminCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.RequestUtil;
import commonsos.util.UserUtil;
import commonsos.view.UserTokenBalanceView;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;

public class UpdateUserCommunitiesByAdminController extends AfterAdminLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected UserView handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateUserCommunitiesByAdminCommand command = gson.fromJson(request.body(), UpdateUserCommunitiesByAdminCommand.class);
    command.setId(RequestUtil.getPathParamLong(request, "id"));
    
    User user = userService.updateUserCommunitiesByAdmin(admin, command);
    List<UserTokenBalanceView> balanceList = userService.balanceViewList(user);
    return UserUtil.wideViewForAdmin(user, balanceList);
  }
}
