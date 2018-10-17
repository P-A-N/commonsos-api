package commonsos.controller.message;

import commonsos.controller.AfterLoginController;
import commonsos.repository.user.User;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

import static java.lang.Long.parseLong;

public class MessageThreadController extends AfterLoginController {
  @Inject MessageService service;

  @Override protected MessageThreadView handle(User user, Request request, Response response) {
    return service.thread(user, parseLong(request.params("id")));
  }
}