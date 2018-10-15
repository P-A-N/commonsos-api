package commonsos.controller.user;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isNotBlank;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.community.CommunityService;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;

public class UserController extends Controller {

  @Inject private UserService userService;
  @Inject private CommunityService communityService;

  @Override public Object handle(User user, Request request, Response response) {
    if (isNotBlank(request.params("id"))) {
      Long requestedUserId = parseLong(request.params("id"));
      User requestedUser = userService.user(requestedUserId);
      return communityService.isAdmin(user.getId(), requestedUser.getCommunityId()) ? userService.privateView(user, requestedUserId) : userService.view(requestedUserId);
    }
    return userService.privateView(user);
  }
}
