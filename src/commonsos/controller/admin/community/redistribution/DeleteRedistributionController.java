package commonsos.controller.admin.community.redistribution;

import static commonsos.annotation.SyncObject.REDISTRIBUTION;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.RedistributionService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

@Synchronized(REDISTRIBUTION)
public class DeleteRedistributionController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject RedistributionService redistributionService;

  @Override
  public Object handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");
    Long redistributionId = RequestUtil.getPathParamLong(request, "redistributionId");
    
    redistributionService.deleteRedistribution(admin, communityId, redistributionId);
    return "";
  }
}
