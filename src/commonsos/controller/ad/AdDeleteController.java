package commonsos.controller.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import spark.Request;
import spark.Response;

public class AdDeleteController extends AfterLoginController {

  @Inject AdService adService;

  @Override public Object handleAfterLogin(User user, Request request, Response response) {
    adService.deleteAdLogically(parseLong(request.params("id")), user);
    return "";
  }
}
