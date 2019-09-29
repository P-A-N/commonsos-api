package commonsos.controller.app.message;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.GroupMessageThreadUpdateCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

public class GroupMessageThreadUpdateController extends AfterAppLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override
  protected MessageThreadView handleAfterLogin(User user, Request request, Response response) {
    GroupMessageThreadUpdateCommand command = gson.fromJson(request.body(), GroupMessageThreadUpdateCommand.class);
    command.setThreadId(parseLong(request.params("id")));
    return service.updateGroup(user, command);
  }
}
