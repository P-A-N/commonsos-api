package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.UserPasswordResetRequestCommand;
import commonsos.view.app.PrivateUserView;
import spark.Request;
import spark.Response;

public class UserPasswordResetRequestController extends AfterLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override public PrivateUserView handleAfterLogin(User user, Request request, Response response) {
    UserPasswordResetRequestCommand command = gson.fromJson(request.body(), UserPasswordResetRequestCommand.class);
    userService.userPasswordResetRequest(user, command);
    return null;
  }
}
