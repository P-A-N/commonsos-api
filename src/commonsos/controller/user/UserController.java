package commonsos.controller.user;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isBlank;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.UserUtil;
import spark.Request;
import spark.Response;

public class UserController extends AfterLoginController {

  @Inject private UserService userService;

  @Override public Object handle(User user, Request request, Response response) {
    if (isBlank(request.params("id"))) return userService.privateView(user);
    
    Long id = parseLong(request.params("id"));
    User requestedUser = userService.user(id);
    
    if (UserUtil.isAdminOfUser(user, requestedUser)) return userService.privateView(user, id);
    else return userService.view(id);
  }
}
