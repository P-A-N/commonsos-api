package commonsos.controller.app.message;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

public class GetMessageThreadController extends AfterAppLoginController {
  @Inject MessageService service;

  @Override
  protected MessageThreadView handleAfterLogin(User user, Request request, Response response) {
    return service.thread(user, parseLong(request.params("id")));
  }
}