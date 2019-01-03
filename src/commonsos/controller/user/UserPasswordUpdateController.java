package commonsos.controller.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.UserPasswordUpdateCommand;
import commonsos.view.UserPrivateView;
import spark.Request;
import spark.Response;

public class UserPasswordUpdateController extends AfterLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override public UserPrivateView handleAfterLogin(User user, Request request, Response response) {
    UserPasswordUpdateCommand command = gson.fromJson(request.body(), UserPasswordUpdateCommand.class);
    User updatedUser = userService.updatePassword(user, command);
    return userService.privateView(updatedUser);
  }
}
