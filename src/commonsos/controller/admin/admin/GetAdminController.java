package commonsos.controller.admin.admin;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.AdminUtil;
import commonsos.util.RequestUtil;
import commonsos.view.admin.AdminView;
import spark.Request;
import spark.Response;

@ReadOnly
public class GetAdminController extends AfterAdminLoginController {

  @Inject AdminService adminService;
  
  @Override
  protected AdminView handleAfterLogin(Admin admin, Request request, Response response) {
    Long id = RequestUtil.getPathParamLong(request, "id");
    
    Admin targetAdmin = adminService.getAdmin(admin, id);
    return AdminUtil.toView(targetAdmin);
  }
}
