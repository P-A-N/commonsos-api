package commonsos.controller.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.user.User;
import commonsos.service.ad.AdService;
import commonsos.service.ad.AdView;
import spark.Request;
import spark.Response;

public class AdController extends AfterLoginController {
  @Inject AdService service;

  @Override public AdView handle(User user, Request request, Response response) {
    return service.view(user, parseLong(request.params("id")));
  }
}
