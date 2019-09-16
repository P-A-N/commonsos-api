package commonsos.controller.app;

import commonsos.ApiVersion;
import commonsos.annotation.ReadOnly;
import commonsos.controller.AbstractController;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

@ReadOnly
public class GetAppApiVersionController extends AbstractController {

  @Override
  public CommonView handle(Request request, Response response) {
    return new CommonView().setApiVersion(ApiVersion.APP_API_VERSION.toString());
  }
}