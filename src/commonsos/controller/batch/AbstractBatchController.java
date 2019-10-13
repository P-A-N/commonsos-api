package commonsos.controller.batch;

import javax.inject.Inject;

import commonsos.Configuration;
import commonsos.ThreadValue;
import commonsos.controller.AbstractController;
import commonsos.exception.AuthenticationException;
import spark.Request;
import spark.Response;

public abstract class AbstractBatchController extends AbstractController {
  
  @Inject Configuration config;

  @Override
  public Object handle(Request request, Response response) {
    checkAccessIp(request);

    ThreadValue.setRequestedBy("BATCH");
    
    handleBatch(request, response);
    return "Success";
  }
  
  private void checkAccessIp(Request request) {
    String accessIp = request.ip();
    if ("localhost".equals(accessIp) || "127.0.0.1".equals(accessIp)) return;
    
    throw new AuthenticationException(String.format("Access from disallowed IP. IP=%s", accessIp));
  }

  abstract protected void handleBatch(Request request, Response response);
}
