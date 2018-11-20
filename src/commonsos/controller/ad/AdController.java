package commonsos.controller.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

@ReadOnly
public class AdController extends AfterLoginController {
  @Inject AdService service;

  @Override public AdView handleAfterLogin(User user, Request request, Response response) {
    return service.view(user, parseLong(request.params("id")));
  }
}
