package commonsos.controller.admin.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import java.util.List;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.command.admin.UpdateUserNameByAdminCommand;
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

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class UpdateUserNameController extends AfterAdminLoginController {

  @Inject UserService userService;
  @Inject Gson gson;
  
  @Override
  protected UserView handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateUserNameByAdminCommand command = gson.fromJson(request.body(), UpdateUserNameByAdminCommand.class);
    command.setId(RequestUtil.getPathParamLong(request, "id"));
    
    User user = userService.updateUserNameByAdmin(admin, command);
    List<UserTokenBalanceView> balanceList = userService.balanceViewList(user);
    return UserUtil.wideViewForAdmin(user, balanceList);
  }
}
