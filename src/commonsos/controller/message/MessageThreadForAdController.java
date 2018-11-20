package commonsos.controller.message;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

import static java.lang.Long.parseLong;

public class MessageThreadForAdController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected MessageThreadView handleAfterLogin(User user, Request request, Response response) {
    return service.threadForAd(user, parseLong(request.params("adId")));
  }
}
