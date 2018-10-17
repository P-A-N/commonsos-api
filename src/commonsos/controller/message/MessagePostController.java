package commonsos.controller.message;

import com.google.gson.Gson;
import commonsos.controller.AfterLoginController;
import commonsos.repository.user.User;
import commonsos.service.message.MessagePostCommand;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class MessagePostController extends AfterLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageView handle(User user, Request request, Response response) {
    return service.postMessage(user, gson.fromJson(request.body(), MessagePostCommand.class));
  }
}
