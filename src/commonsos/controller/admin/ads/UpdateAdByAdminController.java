package commonsos.controller.admin.ads;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.admin.UpdateAdByAdminCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.CommunityService;
import commonsos.service.UserService;
import commonsos.util.AdUtil;
import commonsos.util.RequestUtil;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

public class UpdateAdByAdminController extends AfterAdminLoginController {

  @Inject AdService adService;
  @Inject UserService userService;
  @Inject CommunityService communityService;
  @Inject Gson gson;

  @Override
  protected AdView handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateAdByAdminCommand command = gson.fromJson(request.body(), UpdateAdByAdminCommand.class);
    if (command == null) command = new UpdateAdByAdminCommand();
    command.setId(RequestUtil.getPathParamLong(request, "id"));
    
    Ad ad = adService.updateAdByAdmin(admin, command);
    User creator = userService.getUser(ad.getCreatedUserId());
    Community community = communityService.getCommunity(ad.getCommunityId());

    return AdUtil.viewForAdmin(ad, creator, community);
  }
}
