package commonsos.controller.message;

import com.google.gson.Gson;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.GroupMessageThreadUpdateCommand;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

import static java.lang.Long.parseLong;

public class GroupMessageThreadUpdateController extends AfterLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageThreadView handle(User user, Request request, Response response) {
    GroupMessageThreadUpdateCommand command = gson.fromJson(request.body(), GroupMessageThreadUpdateCommand.class);
    command.setThreadId(parseLong(request.params("id")));
    return service.updateGroup(user, command);
  }
}
