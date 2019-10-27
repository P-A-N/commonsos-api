package commonsos.controller.app;

import static commonsos.controller.app.auth.AppLoginController.USER_SESSION_ATTRIBUTE_NAME;

import javax.inject.Inject;

import commonsos.ThreadValue;
import commonsos.exception.AuthenticationException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.session.UserSession;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public abstract class AfterAppLoginController extends AbstractAppController {

  @Inject UserService userService;

  @Override
  public CommonView handleApp(Request request, Response response) {
    if (request.session().attribute(USER_SESSION_ATTRIBUTE_NAME) == null) throw new AuthenticationException();
    
    UserSession session = request.session().attribute(USER_SESSION_ATTRIBUTE_NAME);
    User user = userService.getUser(session.getUserId());
    
    ThreadValue.setRequestedBy(String.format("APP USER. [userId=%d]", user.getId()));
    
    return handleAfterLogin(user, request, response);
  }

  abstract protected CommonView handleAfterLogin(User user, Request request, Response response);
}
