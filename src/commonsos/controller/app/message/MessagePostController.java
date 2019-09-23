package commonsos.controller.app.message;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.MessagePostCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.app.MessageView;
import spark.Request;
import spark.Response;

public class MessagePostController extends AfterAppLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override
  protected MessageView handleAfterLogin(User user, Request request, Response response) {
    return service.postMessage(user, gson.fromJson(request.body(), MessagePostCommand.class));
  }
}
