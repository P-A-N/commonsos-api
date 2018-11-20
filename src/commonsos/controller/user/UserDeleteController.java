package commonsos.controller.user;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import spark.Request;
import spark.Response;

public class UserDeleteController extends AfterLoginController {

  @Inject UserService userService;

  @Override public Object handleAfterLogin(User user, Request request, Response response) {
    userService.deleteUserLogically(user);
    return "";
  }
}
