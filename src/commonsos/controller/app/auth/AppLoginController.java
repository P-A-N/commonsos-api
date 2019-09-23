package commonsos.controller.app.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;

import javax.inject.Inject;

import org.slf4j.MDC;

import com.google.gson.Gson;

import commonsos.command.app.AppLoginCommand;
import commonsos.controller.app.AbstractAppController;
import commonsos.filter.CSRF;
import commonsos.filter.LogFilter;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;
import spark.Session;

public class AppLoginController extends AbstractAppController {

  public static final String USER_SESSION_ATTRIBUTE_NAME = "user";
  @Inject Gson gson;
  @Inject UserService userService;
  @Inject CSRF csrf;

  @Override
  public UserView handleApp(Request request, Response response) {
    AppLoginCommand command = gson.fromJson(request.body(), AppLoginCommand.class);
    User user = userService.checkPassword(command.getUsername(), command.getPassword());
    user = userService.updateLoggedinAt(user);
    
    Session session = request.session();
    session.attribute(USER_SESSION_ATTRIBUTE_NAME, userService.session(user));
    session.maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);
    
    MDC.put(LogFilter.USER_MDC_KEY, user.getUsername());
    csrf.setToken(request, response);
    
    return userService.privateView(user);
  }
}
