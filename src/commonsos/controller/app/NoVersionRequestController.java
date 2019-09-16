package commonsos.controller.app;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AbstractController;
import commonsos.exception.ServiceUnavailableException;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

@ReadOnly
public class NoVersionRequestController extends AbstractController {

  @Override
  public CommonView handle(Request request, Response response) {
    throw new ServiceUnavailableException();
  }
}
