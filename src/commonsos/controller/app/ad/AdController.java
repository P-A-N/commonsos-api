package commonsos.controller.app.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.app.AdView;
import spark.Request;
import spark.Response;

@ReadOnly
public class AdController extends AfterAppLoginController {
  @Inject AdService service;

  @Override public AdView handleAfterLogin(User user, Request request, Response response) {
    return service.view(user, parseLong(request.params("id")));
  }
}
