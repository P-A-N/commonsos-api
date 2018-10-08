package commonsos.controller.auth;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isNotBlank;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.domain.auth.User;
import commonsos.domain.auth.UserService;
import spark.Request;
import spark.Response;

public class UserController extends Controller {

  @Inject private UserService userService;

  @Override public Object handle(User user, Request request, Response response) {
    if (isNotBlank(request.params("id"))) {
      Long requestedUserId = parseLong(request.params("id"));
      return user.isAdmin() ? userService.privateView(user, requestedUserId) : userService.view(requestedUserId);
    }
    return userService.privateView(user);
  }
}
