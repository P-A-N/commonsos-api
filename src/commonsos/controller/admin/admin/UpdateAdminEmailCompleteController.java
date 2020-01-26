package commonsos.controller.admin.admin;

import static commonsos.annotation.SyncObject.ADMINNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import commonsos.annotation.Synchronized;
import commonsos.controller.AbstractController;
import commonsos.service.AdminService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

@Synchronized(ADMINNAME_AND_EMAIL_ADDRESS)
public class UpdateAdminEmailCompleteController extends AbstractController {

  @Inject AdminService adminService;

  @Override
  public Object handle(Request request, Response response) {
    Long adminId = RequestUtil.getPathParamLong(request, "id");
    String accessId = RequestUtil.getPathParamString(request, "accessId");

    adminService.updateAdminEmailAddressComplete(adminId, accessId);

    return "";
  }
}
