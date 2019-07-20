package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.UserUpdateCommunitiesCommand;
import commonsos.view.app.PrivateUserView;
import spark.Request;
import spark.Response;

public class UserUpdateCommunitiesController extends AfterLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected PrivateUserView handleAfterLogin(User user, Request request, Response response) {
    UserUpdateCommunitiesCommand command = gson.fromJson(request.body(), UserUpdateCommunitiesCommand.class);
    User updatedUser = userService.updateUserCommunities(user, command);
    return userService.privateView(updatedUser);
  }
}
