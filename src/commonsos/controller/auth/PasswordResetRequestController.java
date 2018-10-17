package commonsos.controller.auth;

import java.util.Map;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.exception.BadRequestException;
import spark.Request;
import spark.Response;
import spark.Route;

public class PasswordResetRequestController implements Route {

  @Inject Gson gson;

  @Override public String handle(Request request, Response response) {
    Map<String, String> map = gson.fromJson(request.body(), Map.class);
    if(map == null || !map.containsKey("emailAddress")) throw new BadRequestException("emailAddress is required");
    
    return "";
  }
}
