package commonsos.controller.admin.admin;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.admin.AdminListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchAdminsController extends AfterAdminLoginController {

  @Inject AdminService adminService;
  
  @Override
  public AdminListView handleAfterLogin(Admin admin, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    Long roleId = RequestUtil.getQueryParamLong(request, "roleId", true);
    
    AdminListView view = adminService.searchAdmin(admin, communityId, roleId, paginationCommand);
    return view;
  }
}
