package commonsos.controller.admin.community.redistribution;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.RedistributionService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.RedistributionListView;
import spark.Request;
import spark.Response;

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
