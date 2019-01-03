package commonsos.controller.message;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MessageThreadController extends AfterLoginController {
  @Inject MessageService service;

  @Override protected MessageThreadView handleAfterLogin(User user, Request request, Response response) {
    return service.thread(user, parseLong(request.params("id")));
  }
}