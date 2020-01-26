package commonsos.controller.admin.admin;

import static commonsos.annotation.SyncObject.ADMINNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.command.UpdateEmailAddressTemporaryCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

@Synchronized(ADMINNAME_AND_EMAIL_ADDRESS)
public class UpdateAdminEmailTemporaryController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject AdminService adminService;

  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateEmailAddressTemporaryCommand command = gson.fromJson(request.body(), UpdateEmailAddressTemporaryCommand.class);
    command.setId(RequestUtil.getPathParamLong(request, "id"));
    
    adminService.updateAdminEmailAddressTemporary(admin, command);
    
    return "";
  }
}
