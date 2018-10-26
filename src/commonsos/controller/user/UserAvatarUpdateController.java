package commonsos.controller.user;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

@Slf4j
public class UserAvatarUpdateController extends AfterLoginController {

  @Inject UserService userService;

  @Override public String handle(User user, Request request, Response response) {
    return userService.updateAvatar(user, image(request));
  }
}
