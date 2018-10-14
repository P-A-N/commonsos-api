package commonsos.controller.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.ad.AdService;
import spark.Request;
import spark.Response;

public class AdDeleteController extends Controller {

  @Inject AdService adService;

  @Override public Object handle(User user, Request request, Response response) {
    adService.deleteAdLogically(parseLong(request.params("id")), user);
    return "";
  }
}
