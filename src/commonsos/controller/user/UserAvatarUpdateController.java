package commonsos.controller.user;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

@Slf4j
public class UserAvatarUpdateController extends Controller {

  @Inject UserService userService;

  @Override public String handle(User user, Request request, Response response) {
    return userService.updateAvatar(user, image(request));
  }
}
