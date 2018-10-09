package commonsos.controller.ad;

import java.util.List;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.domain.ad.AdService;
import commonsos.domain.ad.AdView;
import commonsos.domain.auth.User;
import spark.Request;
import spark.Response;

public class MyAdsController extends Controller {
  @Inject AdService service;

  @Override public List<AdView> handle(User user, Request request, Response response) {
    return service.myAdsView(user);
  }
}
