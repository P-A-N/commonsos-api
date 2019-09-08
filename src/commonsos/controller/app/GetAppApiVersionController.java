package commonsos.controller.app;

import commonsos.annotation.ReadOnly;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

@ReadOnly
public class GetAppApiVersionController extends AbstcactAppController {

  @Override
  public CommonView handleApp(Request request, Response response) {
    return new CommonView();
  }
}
