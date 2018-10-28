package commonsos.controller.auth;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.service.UserService;
import commonsos.service.command.CreateAccountTemporaryCommand;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateAccountTemporaryController implements Route {

  @Inject Gson gson;
  @Inject UserService userService;

  @Override public Object handle(Request request, Response response) {
    CreateAccountTemporaryCommand command = gson.fromJson(request.body(), CreateAccountTemporaryCommand.class);
    userService.createAccountTemporary(command);
    
    return null;
  }
}
