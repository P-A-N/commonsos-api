package commonsos.controller.app.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;
import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;
import static commonsos.controller.app.auth.AppLoginController.USER_SESSION_ATTRIBUTE_NAME;

import javax.inject.Inject;

import org.slf4j.MDC;

import commonsos.annotation.Synchronized;
import commonsos.exception.BadRequestException;
import commonsos.filter.CSRF;
import commonsos.filter.LogFilter;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.app.PrivateUserView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class CreateAccountCompleteController implements Route {

  @Inject UserService userService;
  @Inject CSRF csrf;
  
  @Override public PrivateUserView handle(Request request, Response response) {
    String accessId = request.params("accessId");
    if(accessId == null || accessId.isEmpty()) throw new BadRequestException("accessId is required");

    User user = userService.createAccountComplete(accessId);

    Session session = request.session();
    session.attribute(USER_SESSION_ATTRIBUTE_NAME, userService.session(user));
    session.maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);

    MDC.put(LogFilter.USER_MDC_KEY, user.getUsername());
    csrf.setToken(request, response);

    return userService.privateView(user);
  }
}
