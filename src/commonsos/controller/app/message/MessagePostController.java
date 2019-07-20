package commonsos.controller.app.message;

import com.google.gson.Gson;

import commonsos.controller.app.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.MessagePostCommand;
import commonsos.view.app.MessageView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class MessagePostController extends AfterLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageView handleAfterLogin(User user, Request request, Response response) {
    return service.postMessage(user, gson.fromJson(request.body(), MessagePostCommand.class));
  }
}
