package commonsos.controller.admin.admin;

import static commonsos.annotation.SyncObject.ADMINNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.command.admin.UpdateAdminCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.AdminUtil;
import commonsos.util.RequestUtil;
import commonsos.view.AdminView;
import spark.Request;
import spark.Response;

@Synchronized(ADMINNAME_AND_EMAIL_ADDRESS)
public class UpdateAdminController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject AdminService adminService;
  
  @Override
  protected AdminView handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateAdminCommand command = gson.fromJson(request.body(), UpdateAdminCommand.class);
    command.setAdminId(RequestUtil.getPathParamLong(request, "id"));

    Admin targetAdmin = adminService.updateAdmin(admin, command);
    return AdminUtil.view(targetAdmin);
  }
}
