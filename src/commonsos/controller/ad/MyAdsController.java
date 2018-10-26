package commonsos.controller.ad;

import java.util.List;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

public class MyAdsController extends AfterLoginController {
  @Inject AdService service;

  @Override public List<AdView> handle(User user, Request request, Response response) {
    return service.myAdsView(user);
  }
}
