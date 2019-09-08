package commonsos.controller.app;

import commonsos.ApiVersion;
import commonsos.controller.AbstractController;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public abstract class AbstcactAppController extends AbstractController {

  @Override
  public Object handle(Request request, Response response) {
    CommonView view = handleApp(request, response);
    return view.setApiVersion(ApiVersion.APP_API_VERSION.toString());
  }

  abstract protected CommonView handleApp(Request request, Response response);
}
