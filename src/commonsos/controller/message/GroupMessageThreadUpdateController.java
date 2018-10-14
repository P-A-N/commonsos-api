package commonsos.controller.message;

import com.google.gson.Gson;
import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.message.GroupMessageThreadUpdateCommand;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

import static java.lang.Long.parseLong;

public class GroupMessageThreadUpdateController extends Controller {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageThreadView handle(User user, Request request, Response response) {
    GroupMessageThreadUpdateCommand command = gson.fromJson(request.body(), GroupMessageThreadUpdateCommand.class);
    command.setThreadId(parseLong(request.params("id")));
    return service.updateGroup(user, command);
  }
}
