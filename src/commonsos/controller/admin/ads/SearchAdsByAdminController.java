package commonsos.controller.admin.ads;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.AdService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.AdListView;
import spark.Request;
import spark.Response;

public class SearchAdsByAdminController extends AfterAdminLoginController {

  @Inject AdService adService;

  @Override
  protected AdListView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    AdListView view = adService.searchAdsByAdmin(admin, communityId, paginationCommand);
    return view;
  }
}
