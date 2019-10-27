package commonsos.controller.admin.user;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.DeleteService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public class DeleteUserByAdminController extends AfterAdminLoginController {

  @Inject DeleteService deleteService;

  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    Long id = RequestUtil.getPathParamLong(request, "id");
    deleteService.deleteUserByAdmin(admin, id);
    return "";
  }
}
