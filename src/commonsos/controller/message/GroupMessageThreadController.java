package commonsos.controller.message;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.CreateGroupCommand;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

public class GroupMessageThreadController extends AfterLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageThreadView handle(User user, Request request, Response response) {
    CreateGroupCommand command = gson.fromJson(request.body(), CreateGroupCommand.class);
    return service.group(user, command);
  }
}
