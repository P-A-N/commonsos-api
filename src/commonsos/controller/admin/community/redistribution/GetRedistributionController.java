package commonsos.controller.admin.community.redistribution;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Redistribution;
import commonsos.service.RedistributionService;
import commonsos.util.RedistributionUtil;
import commonsos.util.RequestUtil;
import commonsos.view.RedistributionView;
import spark.Request;
import spark.Response;

public class GetRedistributionController extends AfterAdminLoginController {

  @Inject RedistributionService redistributionService;

  @Override
  public RedistributionView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");
    Long redistributionId = RequestUtil.getPathParamLong(request, "redistributionId");
    
    Redistribution redistribution = redistributionService.getRedistribution(admin, redistributionId, communityId);
    return RedistributionUtil.toView(redistribution);
  }
}
