package commonsos.controller.admin.community;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.service.CommunityService;
import commonsos.util.RequestUtil;
import commonsos.view.admin.CommunityForAdminView;
import spark.Request;
import spark.Response;

@ReadOnly
public class GetCommunityController extends AfterAdminLoginController {

  @Inject CommunityService communityService;

  @Override
  public CommunityForAdminView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");
    Community community = communityService.findCommunityForAdmin(communityId);
    return communityService.viewForAdmin(community);
  }
}
