package commonsos.controller.admin.admin;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public class DeleteAdminController extends AfterAdminLoginController {

  @Inject AdminService adminService;
  
  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    Long adminId = RequestUtil.getPathParamLong(request, "id");
    adminService.deleteAdmin(admin, adminId);
    
    return "";
  }
}
