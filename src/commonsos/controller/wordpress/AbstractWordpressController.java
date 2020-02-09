package commonsos.controller.wordpress;

import javax.inject.Inject;

import commonsos.Configuration;
import commonsos.ThreadValue;
import commonsos.controller.AbstractController;
import commonsos.exception.AuthenticationException;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public abstract class AbstractWordpressController extends AbstractController {
  
  @Inject Configuration config;

  @Override
  public Object handle(Request request, Response response) {
    checkAccessIp(request);

    ThreadValue.setRequestedBy("WORDPRESS");
    
    CommonView view = handleWordpress(request, response);
    return view;
  }
  
  private void checkAccessIp(Request request) {
    String accessIp = request.ip();
    
    String AllowIps = config.allowedWordpressRequestIpList();
    for (String allowIp : AllowIps.split(",")) {
      if (allowIp.equals(accessIp)) return;
    }
    
    throw new AuthenticationException(String.format("Access from disallowed IP. IP=%s", accessIp));
  }

  abstract protected CommonView handleWordpress(Request request, Response response);
}
