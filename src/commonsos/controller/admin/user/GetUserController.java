package commonsos.controller.admin.user;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.AdminUtil;
import commonsos.util.RequestUtil;
import commonsos.util.UserUtil;
import commonsos.view.UserTokenBalanceView;
import commonsos.view.admin.UserForAdminView;
import spark.Request;
import spark.Response;

@ReadOnly
public class GetUserController extends AfterAdminLoginController {

  @Inject UserService userService;
  
  @Override
  protected UserForAdminView handleAfterLogin(Admin admin, Request request, Response response) {
    Long id = RequestUtil.getPathParamLong(request, "id");
    
    User user = userService.user(id);
    if (!AdminUtil.isSeeable(admin, user)) throw new ForbiddenException();
    List<UserTokenBalanceView> balanceList = userService.balanceViewList(user);
    
    return UserUtil.userForAdminView(user, balanceList);
  }
}
