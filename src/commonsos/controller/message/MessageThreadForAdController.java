package commonsos.controller.message;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

import static java.lang.Long.parseLong;

public class MessageThreadForAdController extends Controller {

  @Inject MessageService service;

  @Override protected MessageThreadView handle(User user, Request request, Response response) {
    return service.threadForAd(user, parseLong(request.params("adId")));
  }
}
