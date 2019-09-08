package commonsos.controller.app.auth;

import static commonsos.controller.app.auth.AppLoginController.USER_SESSION_ATTRIBUTE_NAME;

import commonsos.controller.app.AbstractAppController;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;
import spark.Session;

public class AppLogoutController extends AbstractAppController {
  
  @Override
  public CommonView handleApp(Request request, Response response) {
    Session session = request.session(false);
    if (session != null) session.removeAttribute(USER_SESSION_ATTRIBUTE_NAME);
    return new CommonView();
  }
}
