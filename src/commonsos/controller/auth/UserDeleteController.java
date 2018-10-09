package commonsos.controller.auth;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.domain.auth.User;
import commonsos.domain.auth.UserService;
import spark.Request;
import spark.Response;

public class UserDeleteController extends Controller {

  @Inject UserService userService;

  @Override public Object handle(User user, Request request, Response response) {
    userService.deleteUserLogically(user);
    return "";
  }
}
