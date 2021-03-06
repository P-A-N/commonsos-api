package commonsos.controller.ad;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.AdListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MyAdsController extends AfterLoginController {
  @Inject AdService service;

  @Override public AdListView handleAfterLogin(User user, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    AdListView view = service.myAdsView(user, paginationCommand);
    return view;
  }
}
