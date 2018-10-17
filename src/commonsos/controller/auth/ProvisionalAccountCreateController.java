package commonsos.controller.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;
import static commonsos.controller.auth.LoginController.USER_SESSION_ATTRIBUTE_NAME;

import javax.inject.Inject;

import org.slf4j.MDC;

import com.google.gson.Gson;

import commonsos.CSRF;
import commonsos.LogFilter;
import commonsos.repository.user.User;
import commonsos.service.auth.ProvisionalAccountCreateCommand;
import commonsos.service.user.UserPrivateView;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

public class ProvisionalAccountCreateController implements Route {

  @Inject Gson gson;
  @Inject UserService userService;
  @Inject CSRF csrf;

  @Override public String handle(Request request, Response response) {
    ProvisionalAccountCreateCommand command = gson.fromJson(request.body(), ProvisionalAccountCreateCommand.class);
    User user = userService.create(command);

    Session session = request.session();
    session.attribute(USER_SESSION_ATTRIBUTE_NAME, userService.session(user));
    session.maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);

    MDC.put(LogFilter.USERNAME_MDC_KEY, user.getUsername());
    csrf.setToken(request, response);
    
    UserPrivateView privateView = userService.privateView(user);
    return "";
  }
}
