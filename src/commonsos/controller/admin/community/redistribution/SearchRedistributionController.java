package commonsos.controller.admin.community.redistribution;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.Admin;
import commonsos.service.RedistributionService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.admin.RedistributionListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchRedistributionController extends AfterAdminLoginController {

  @Inject RedistributionService redistributionService;

  @Override
  public RedistributionListView handleAfterLogin(Admin admin, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    Long communityId = RequestUtil.getPathParamLong(request, "id");
    
    RedistributionListView view = redistributionService.searchRedistribution(admin, communityId, paginationCommand);
    return view;
  }
}
