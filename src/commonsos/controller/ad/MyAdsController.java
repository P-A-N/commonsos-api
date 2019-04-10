package commonsos.controller.ad;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.AdListView;
import commonsos.view.AdView;
import commonsos.view.PaginationView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MyAdsController extends AfterLoginController {
  @Inject AdService service;

  @Override public AdListView handleAfterLogin(User user, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    List<AdView> adList = service.myAdsView(user);
    PaginationView paginationView = PaginationUtil.toView(paginationCommand);
    AdListView view = new AdListView()
        .setAdList(adList)
        .setPagination(paginationView);
    
    return view;
  }
}
