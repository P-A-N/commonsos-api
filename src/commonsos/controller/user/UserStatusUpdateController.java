package commonsos.controller.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.UserStatusUpdateCommand;
import commonsos.view.UserPrivateView;
import spark.Request;
import spark.Response;

public class UserStatusUpdateController extends AfterLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override public UserPrivateView handleAfterLogin(User user, Request request, Response response) {
    UserStatusUpdateCommand command = gson.fromJson(request.body(), UserStatusUpdateCommand.class);
    User updatedUser = userService.updateStatus(user, command);
    return userService.privateView(updatedUser);
  }
}
