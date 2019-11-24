package commonsos.controller.admin.community.redistribution;

import static commonsos.annotation.SyncObject.REDISTRIBUTION;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.command.admin.UpdateRedistributionCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Redistribution;
import commonsos.service.RedistributionService;
import commonsos.util.RedistributionUtil;
import commonsos.util.RequestUtil;
import commonsos.view.RedistributionView;
import spark.Request;
import spark.Response;

@Synchronized(REDISTRIBUTION)
public class UpdateRedistributionController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject RedistributionService redistributionService;

  @Override
  public RedistributionView handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateRedistributionCommand command = gson.fromJson(request.body(), UpdateRedistributionCommand.class);
    command.setCommunityId(RequestUtil.getPathParamLong(request, "id"));
    command.setRedistributionId(RequestUtil.getPathParamLong(request, "redistributionId"));
    
    Redistribution redistribution = redistributionService.updateRedistribution(admin, command);
    return RedistributionUtil.toView(redistribution);
  }
}
