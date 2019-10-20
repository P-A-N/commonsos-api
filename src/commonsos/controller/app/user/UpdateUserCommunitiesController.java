package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UpdateUserCommunitiesCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;

public class UpdateUserCommunitiesController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected UserView handleAfterLogin(User user, Request request, Response response) {
    UpdateUserCommunitiesCommand command = gson.fromJson(request.body(), UpdateUserCommunitiesCommand.class);
    User updatedUser = userService.updateUserCommunities(user, command);
    return userService.privateView(updatedUser);
  }
}
