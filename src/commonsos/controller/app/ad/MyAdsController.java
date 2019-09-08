package commonsos.controller.app.ad;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.app.AdListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MyAdsController extends AfterAppLoginController {
  @Inject AdService service;

  @Override
  public AdListView handleAfterLogin(User user, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    AdListView view = service.myAdsView(user, paginationCommand);
    return view;
  }
}
