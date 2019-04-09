package commonsos.controller.ad;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.AdListView;
import commonsos.view.AdView;
import commonsos.view.PagenationView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MyAdsController extends AfterLoginController {
  @Inject AdService service;

  @Override public AdListView handleAfterLogin(User user, Request request, Response response) {
    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);
    
    List<AdView> adList = service.myAdsView(user);
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    AdListView view = new AdListView()
        .setAdList(adList)
        .setPagenation(pagenationView);
    
    return view;
  }
}
