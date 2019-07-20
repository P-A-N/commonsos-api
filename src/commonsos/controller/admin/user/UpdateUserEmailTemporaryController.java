package commonsos.controller.admin.user;

import java.util.HashMap;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class UpdateUserEmailTemporaryController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    
    return result;
  }
}
