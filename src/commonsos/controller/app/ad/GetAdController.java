package commonsos.controller.app.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

public class GetAdController extends AfterAppLoginController {
  @Inject AdService service;

  @Override public AdView handleAfterLogin(User user, Request request, Response response) {
    return service.view(user, parseLong(request.params("id")));
  }
}
