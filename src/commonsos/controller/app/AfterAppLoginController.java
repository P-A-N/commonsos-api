package commonsos.controller.app;

import static commonsos.controller.app.auth.AppLoginController.USER_SESSION_ATTRIBUTE_NAME;

import javax.inject.Inject;

import commonsos.controller.AbstractController;
import commonsos.exception.AuthenticationException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.session.UserSession;
import spark.Request;
import spark.Response;

public abstract class AfterAppLoginController extends AbstractController {

  @Inject UserService userService;

  @Override
  public Object handle(Request request, Response response) {
    if (request.session().attribute(USER_SESSION_ATTRIBUTE_NAME) == null) throw new AuthenticationException();
    
    UserSession session = request.session().attribute(USER_SESSION_ATTRIBUTE_NAME);
    User user = userService.user(session.getUserId());
    return handleAfterLogin(user, request, response);
  }

  abstract protected Object handleAfterLogin(User user, Request request, Response response);
}
