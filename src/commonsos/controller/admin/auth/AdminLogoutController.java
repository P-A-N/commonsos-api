package commonsos.controller.admin.auth;

import static commonsos.controller.admin.auth.AdminLoginController.ADMIN_SESSION_ATTRIBUTE_NAME;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

public class AdminLogoutController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Session session = request.session(false);
    if (session != null) session.removeAttribute(ADMIN_SESSION_ATTRIBUTE_NAME);
    return "";
  }
}
