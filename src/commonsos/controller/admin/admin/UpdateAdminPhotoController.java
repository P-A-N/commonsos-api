package commonsos.controller.admin.admin;

import java.util.HashMap;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class UpdateAdminPhotoController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("photoUrl", "https://commonsos-test.s3.amazonaws.com/2f63ed4c-3ff0-46cf-8358-eb91efcbe9c0");
    
    return result;
  }
}
