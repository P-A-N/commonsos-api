package commonsos.controller.admin.community;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.admin.UpdateCommunityTokenNameCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.service.CommunityService;
import commonsos.util.RequestUtil;
import commonsos.view.CommunityView;
import spark.Request;
import spark.Response;

public class UpdateCommunityTokenNameController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject CommunityService communityService;
  
  @Override
  protected CommunityView handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateCommunityTokenNameCommand command = gson.fromJson(request.body(), UpdateCommunityTokenNameCommand.class);
    command.setCommunityId(RequestUtil.getPathParamLong(request, "id"));

    Community community = communityService.updateTokenName(admin, command);
    return communityService.viewForAdmin(community);
  }
}
