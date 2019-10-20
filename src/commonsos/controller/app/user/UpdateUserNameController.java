package commonsos.controller.app.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.command.app.UpdateUserNameCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class UpdateUserNameController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  public UserView handleAfterLogin(User user, Request request, Response response) {
    UpdateUserNameCommand command = gson.fromJson(request.body(), UpdateUserNameCommand.class);
    User updatedUser = userService.updateUserName(user, command);
    return userService.privateView(updatedUser);
  }
}
