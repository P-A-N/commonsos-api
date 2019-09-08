package commonsos.controller.admin.community.redistribution;

import static commonsos.annotation.SyncObject.REDISTRIBUTION;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.controller.command.admin.CreateRedistributionCommand;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Redistribution;
import commonsos.service.RedistributionService;
import commonsos.util.RedistributionUtil;
import commonsos.util.RequestUtil;
import commonsos.view.admin.RedistributionView;
import spark.Request;
import spark.Response;

@Synchronized(REDISTRIBUTION)
public class CreateRedistributionController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject RedistributionService redistributionService;

  @Override
  public RedistributionView handleAfterLogin(Admin admin, Request request, Response response) {
    CreateRedistributionCommand command = gson.fromJson(request.body(), CreateRedistributionCommand.class);
    command.setCommunityId(RequestUtil.getPathParamLong(request, "id"));
    
    Redistribution redistribution = redistributionService.createRedistribution(admin, command);
    return RedistributionUtil.toView(redistribution);
  }
}
