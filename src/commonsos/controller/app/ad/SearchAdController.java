package commonsos.controller.app.ad;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.AdListView;
import spark.Request;
import spark.Response;

public class SearchAdController extends AfterAppLoginController {
  @Inject AdService service;

  @Override
  public AdListView handleAfterLogin(User user, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    AdListView view = service.searchAds(user, communityId, request.queryParams("filter"), paginationCommand);
    return view;
  }
}
