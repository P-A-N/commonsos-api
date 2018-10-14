package commonsos.controller.message;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.message.CreateGroupCommand;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageThreadView;
import spark.Request;
import spark.Response;

public class GroupMessageThreadController extends Controller {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageThreadView handle(User user, Request request, Response response) {
    CreateGroupCommand command = gson.fromJson(request.body(), CreateGroupCommand.class);
    return service.group(user, command);
  }
}
