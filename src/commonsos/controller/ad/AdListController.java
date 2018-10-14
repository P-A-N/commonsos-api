package commonsos.controller.ad;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.ad.AdService;
import spark.Request;
import spark.Response;

public class AdListController extends Controller {
  @Inject AdService service;

  @Override public Object handle(User user, Request request, Response response) {
    return service.listFor(user, request.queryParams("filter"));
  }
}
