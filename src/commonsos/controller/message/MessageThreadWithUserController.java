package commonsos.controller.message;

import static commonsos.annotation.SyncObject.MESSAGE_THRED_BETWEEN_USER;

import javax.inject.Inject;

import commonsos.annotation.Synchronized;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import spark.Request;
import spark.Response;

@Synchronized(MESSAGE_THRED_BETWEEN_USER)
public class MessageThreadWithUserController extends AfterLoginController {
  @Inject MessageService service;

  @Override protected Object handleAfterLogin(User user, Request request, Response response) {
    return service.threadWithUser(user, Long.parseLong(request.params("userId")));
  }
}