package commonsos.controller.app;

import static commonsos.ApiVersion.APP_API_VERSION;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.Cache;
import commonsos.controller.AbstractController;
import commonsos.exception.ServiceUnavailableException;
import commonsos.exception.UrlNotFoundException;
import commonsos.util.RequestUtil;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public abstract class AbstractAppController extends AbstractController {

  @Inject private Cache cache;

  @Override
  public Object handle(Request request, Response response) {
    String version = RequestUtil.getPathParamString(request, "version");
    if (!version.startsWith("v") || version.contains(".")) throw new UrlNotFoundException(String.format("invalid api version. [url=%s]", request.url()));
    
    version = version.substring(1);
    if (!NumberUtils.isParsable(version)) throw new UrlNotFoundException(String.format("invalid api version. [url=%s]", request.url()));
    
    int majorVersion = Integer.parseInt(version);
    if (APP_API_VERSION.conpareMajorTo(majorVersion) < 0) throw new UrlNotFoundException(String.format("invalid api version. [url=%s]", request.url()));
    if (APP_API_VERSION.conpareMajorTo(majorVersion) > 0) throw new ServiceUnavailableException();

    boolean maintenanceMode = Boolean.valueOf(cache.getSystemConfig(Cache.SYS_CONFIG_KEY_MAINTENANCE_MODE));
    if (maintenanceMode) throw new ServiceUnavailableException();
    
    CommonView view = handleApp(request, response);
    return view.setApiVersion(APP_API_VERSION.toString());
  }

  abstract protected CommonView handleApp(Request request, Response response);
}
