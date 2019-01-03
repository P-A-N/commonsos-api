package commonsos.controller.user;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import spark.Request;
import spark.Response;

public class UserAvatarUpdateController extends AfterLoginController {

  @Inject UserService userService;

  @Override public String handleAfterLogin(User user, Request request, Response response) {
    return userService.updateAvatar(user, image(request));
  }
}
