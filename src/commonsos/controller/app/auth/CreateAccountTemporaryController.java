package commonsos.controller.app.auth;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.service.UserService;
import commonsos.service.command.CreateAccountTemporaryCommand;
import spark.Request;
import spark.Response;
import spark.Route;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class CreateAccountTemporaryController implements Route {

  @Inject Gson gson;
  @Inject UserService userService;

  @Override public Object handle(Request request, Response response) {
    CreateAccountTemporaryCommand command = gson.fromJson(request.body(), CreateAccountTemporaryCommand.class);
    userService.createAccountTemporary(command);
    
    return null;
  }
}
