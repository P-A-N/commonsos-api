package commonsos.controller.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;
import static commonsos.controller.auth.LoginController.USER_SESSION_ATTRIBUTE_NAME;

import javax.inject.Inject;

import org.slf4j.MDC;

import com.google.gson.Gson;

import commonsos.CSRF;
import commonsos.LogFilter;
import commonsos.domain.auth.AccountCreateCommand;
import commonsos.domain.auth.User;
import commonsos.domain.auth.UserPrivateView;
import commonsos.domain.auth.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

public class AccountCreateController implements Route {

  @Inject Gson gson;
  @Inject UserService userService;
  @Inject CSRF csrf;

  @Override public UserPrivateView handle(Request request, Response response) {
    AccountCreateCommand command = gson.fromJson(request.body(), AccountCreateCommand.class);
    User user = userService.create(command);

    Session session = request.session();
    session.attribute(USER_SESSION_ATTRIBUTE_NAME, userService.session(user));
    session.maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);

    MDC.put(LogFilter.USERNAME_MDC_KEY, user.getUsername());
    csrf.setToken(request, response);
    
    return userService.privateView(user);
  }
}
