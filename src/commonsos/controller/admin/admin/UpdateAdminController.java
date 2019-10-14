package commonsos.controller.admin.admin;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.admin.UpdateAdminCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.AdminUtil;
import commonsos.util.RequestUtil;
import commonsos.view.AdminView;
import spark.Request;
import spark.Response;

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
