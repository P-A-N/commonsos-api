package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UserUpdateCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;

public class UserUpdateController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected UserView handleAfterLogin(User user, Request request, Response response) {
    UserUpdateCommand command = gson.fromJson(request.body(), UserUpdateCommand.class);
    User updatedUser = userService.updateUser(user, command);
    return userService.privateView(updatedUser);
  }
}
