package commonsos.controller.user;

import com.google.gson.Gson;
import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import commonsos.service.user.UserUpdateCommand;
import commonsos.service.view.UserPrivateView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class UserUpdateController extends Controller {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override protected UserPrivateView handle(User user, Request request, Response response) {
    UserUpdateCommand command = gson.fromJson(request.body(), UserUpdateCommand.class);
    User updatedUser = userService.updateUser(user, command);
    return userService.privateView(updatedUser);
  }
}
