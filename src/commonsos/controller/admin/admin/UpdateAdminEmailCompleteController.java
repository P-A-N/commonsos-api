package commonsos.controller.admin.admin;

import java.util.HashMap;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class UpdateAdminEmailCompleteController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    
    return result;
  }
}