package commonsos.controller.message;

import commonsos.controller.AfterLoginController;
import commonsos.repository.user.User;
import commonsos.service.message.MessageService;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class MessageThreadWithUserController extends AfterLoginController {
  @Inject MessageService service;

  @Override protected Object handle(User user, Request request, Response response) {
    return service.threadWithUser(user, Long.parseLong(request.params("userId")));
  }
}