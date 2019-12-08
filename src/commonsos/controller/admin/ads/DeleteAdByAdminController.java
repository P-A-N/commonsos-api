package commonsos.controller.admin.ads;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdService;
import commonsos.service.CommunityService;
import commonsos.service.UserService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public class DeleteAdByAdminController extends AfterAdminLoginController {

  @Inject AdService adService;
  @Inject UserService userService;
  @Inject CommunityService communityService;
  @Inject Gson gson;

  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    Long id = RequestUtil.getPathParamLong(request, "id");
    adService.deleteAdLogicallyByAdmin(admin, id);

    return "";
  }
}
