package commonsos.controller.auth;

import java.util.Map;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.exception.BadRequestException;
import spark.Request;
import spark.Response;
import spark.Route;

public class PasswordResetController implements Route {

  @Inject Gson gson;

  @Override public String handle(Request request, Response response) {
    String accessId = request.params("accessId");
    if(accessId == null || accessId.isEmpty()) throw new BadRequestException("accessId is required");
    
    Map<String, String> map = gson.fromJson(request.body(), Map.class);
    if(map == null || !map.containsKey("newPassword")) throw new BadRequestException("newPassword is required");
    
    return null;
  }
}
