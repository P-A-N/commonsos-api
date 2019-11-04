package commonsos.controller.admin.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.UpdateEmailAddressTemporaryCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.UserService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public class UpdateUserEmailTemporaryByAdminController extends AfterAdminLoginController {

  @Inject UserService userService;
  @Inject Gson gson;
  
  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateEmailAddressTemporaryCommand command = gson.fromJson(request.body(), UpdateEmailAddressTemporaryCommand.class);
    command.setId(RequestUtil.getPathParamLong(request, "id"));
    
    userService.updateEmailTemporaryByAdmin(admin, command);
    return "";
  }
}
