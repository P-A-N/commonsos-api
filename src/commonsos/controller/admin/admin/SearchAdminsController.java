package commonsos.controller.admin.admin;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.AdminListView;
import spark.Request;
import spark.Response;

public class SearchAdminsController extends AfterAdminLoginController {

  @Inject AdminService adminService;
  
  @Override
  public AdminListView handleAfterLogin(Admin admin, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", false);
    Long roleId = RequestUtil.getQueryParamLong(request, "roleId", true);
    
    AdminListView view = adminService.searchAdmin(admin, communityId, roleId, paginationCommand);
    return view;
  }
}
