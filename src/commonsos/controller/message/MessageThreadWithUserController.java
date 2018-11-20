package commonsos.controller.message;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class MessageThreadWithUserController extends AfterLoginController {
  @Inject MessageService service;

  @Override protected Object handleAfterLogin(User user, Request request, Response response) {
    return service.threadWithUser(user, Long.parseLong(request.params("userId")));
  }
}