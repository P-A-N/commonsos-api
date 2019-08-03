package commonsos.controller.app.user;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isBlank;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.UserUtil;
import spark.Request;
import spark.Response;

@ReadOnly
public class UserController extends AfterAppLoginController {

  @Inject private UserService userService;

  @Override public Object handleAfterLogin(User user, Request request, Response response) {
    if (isBlank(request.params("id"))) return userService.privateView(user);

    String userId = request.params("id");
    if (!NumberUtils.isParsable(userId)) throw new BadRequestException("invalid userId");
    Long id = parseLong(userId);
    User requestedUser = userService.user(id);
    
    if (UserUtil.isAdminOfUser(user, requestedUser)) return userService.privateView(user, id);
    else return userService.publicUserAndCommunityView(id);
  }
}
