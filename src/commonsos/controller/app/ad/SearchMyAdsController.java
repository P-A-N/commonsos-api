package commonsos.controller.app.ad;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.util.PaginationUtil;
import commonsos.view.AdListView;
import spark.Request;
import spark.Response;

public class SearchMyAdsController extends AfterAppLoginController {
  @Inject AdService service;

  @Override
  public AdListView handleAfterLogin(User user, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    AdListView view = service.myAdsView(user, paginationCommand);
    return view;
  }
}
