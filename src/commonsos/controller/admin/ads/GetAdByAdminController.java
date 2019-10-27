package commonsos.controller.admin.ads;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.CommunityService;
import commonsos.service.UserService;
import commonsos.util.AdUtil;
import commonsos.util.AdminUtil;
import commonsos.util.RequestUtil;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

public class GetAdByAdminController extends AfterAdminLoginController {

  @Inject AdService adService;
  @Inject UserService userService;
  @Inject CommunityService communityService;

  @Override
  protected AdView handleAfterLogin(Admin admin, Request request, Response response) {
    Long id = RequestUtil.getPathParamLong(request, "id");
    
    Ad ad = adService.getAd(id);
    if (!AdminUtil.isSeeableAd(admin, ad.getCommunityId())) throw new ForbiddenException();

    User creator = userService.getUser(ad.getCreatedUserId());
    Community community = communityService.getCommunity(ad.getCommunityId());

    return AdUtil.viewForAdmin(ad, creator, community);
  }
}
