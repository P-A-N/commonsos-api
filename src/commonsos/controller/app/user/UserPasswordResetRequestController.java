package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UserPasswordResetRequestCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public class UserPasswordResetRequestController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  public CommonView handleAfterLogin(User user, Request request, Response response) {
    UserPasswordResetRequestCommand command = gson.fromJson(request.body(), UserPasswordResetRequestCommand.class);
    userService.userPasswordResetRequest(user, command);
    return new CommonView();
  }
}
