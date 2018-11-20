package commonsos.controller.ad;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MyAdsController extends AfterLoginController {
  @Inject AdService service;

  @Override public List<AdView> handleAfterLogin(User user, Request request, Response response) {
    return service.myAdsView(user);
  }
}
