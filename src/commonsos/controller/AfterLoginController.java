package commonsos.controller;

import static commonsos.controller.auth.LoginController.USER_SESSION_ATTRIBUTE_NAME;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import javax.inject.Inject;

import commonsos.AuthenticationException;
import commonsos.UserSession;
import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class AfterLoginController implements Route {

  @Inject UserService userService;

  @Override final public Object handle(Request request, Response response) {
    if (request.session().attribute(USER_SESSION_ATTRIBUTE_NAME) == null) throw new AuthenticationException();
    
    UserSession session = request.session().attribute(USER_SESSION_ATTRIBUTE_NAME);
    User user = userService.user(session.getUserId());
    return handle(user, request, response);
  }

  abstract protected Object handle(User user, Request request, Response response);

  public InputStream image(Request request) {
    String base64 = request.body().replaceFirst("data:image/.*;base64,", "");
    return new ByteArrayInputStream(Base64.getDecoder().decode(base64));
  }
}
