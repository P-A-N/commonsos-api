package commonsos.controller.app.auth;

import static commonsos.controller.app.auth.AppLoginController.USER_SESSION_ATTRIBUTE_NAME;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

public class AppLogoutController implements Route {
  
  @Override
  public Object handle(Request request, Response response) {
    Session session = request.session(false);
    if (session != null) session.removeAttribute(USER_SESSION_ATTRIBUTE_NAME);
    return "";
  }
}
