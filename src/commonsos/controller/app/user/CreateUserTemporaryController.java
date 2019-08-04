package commonsos.controller.app.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.service.UserService;
import commonsos.service.command.CreateUserTemporaryCommand;
import spark.Request;
import spark.Response;
import spark.Route;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class CreateUserTemporaryController implements Route {

  @Inject Gson gson;
  @Inject UserService userService;

  @Override public Object handle(Request request, Response response) {
    CreateUserTemporaryCommand command = gson.fromJson(request.body(), CreateUserTemporaryCommand.class);
    userService.createAccountTemporary(command);
    
    return null;
  }
}
