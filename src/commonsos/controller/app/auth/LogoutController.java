package commonsos.controller.app.auth;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

@ReadOnly
public class LogoutController implements Route {
  @Override public Object handle(Request request, Response response) {
    Session session = request.session(false);
    if (session != null) session.invalidate();
    return "";
  }
}
