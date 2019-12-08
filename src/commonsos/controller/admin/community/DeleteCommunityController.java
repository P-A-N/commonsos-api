package commonsos.controller.admin.community;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.CommunityService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public class DeleteCommunityController extends AfterAdminLoginController {

  @Inject CommunityService communityService;
  
  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    Long id = RequestUtil.getPathParamLong(request, "id");
    communityService.deleteCommunity(admin, id);

    return "";
  }
}
