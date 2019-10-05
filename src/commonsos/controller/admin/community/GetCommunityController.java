package commonsos.controller.admin.community;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.service.CommunityService;
import commonsos.util.RequestUtil;
import commonsos.view.CommunityView;
import spark.Request;
import spark.Response;

public class GetCommunityController extends AfterAdminLoginController {

  @Inject CommunityService communityService;

  @Override
  public CommunityView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");
    Community community = communityService.findCommunityForAdmin(admin, communityId);
    return communityService.viewForAdmin(community);
  }
}
