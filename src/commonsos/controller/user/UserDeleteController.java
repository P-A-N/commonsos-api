package commonsos.controller.user;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;

public class UserDeleteController extends Controller {

  @Inject UserService userService;

  @Override public Object handle(User user, Request request, Response response) {
    userService.deleteUserLogically(user);
    return "";
  }
}
