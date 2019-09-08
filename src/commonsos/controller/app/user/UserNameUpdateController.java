package commonsos.controller.app.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.controller.command.app.UserNameUpdateCommand;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.app.PrivateUserView;
import spark.Request;
import spark.Response;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class UserNameUpdateController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  public PrivateUserView handleAfterLogin(User user, Request request, Response response) {
    UserNameUpdateCommand command = gson.fromJson(request.body(), UserNameUpdateCommand.class);
    User updatedUser = userService.updateUserName(user, command);
    return userService.privateView(updatedUser);
  }
}
