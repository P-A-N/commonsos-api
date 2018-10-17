package commonsos.controller.auth;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.exception.BadRequestException;
import spark.Request;
import spark.Response;
import spark.Route;

public class CheckPasswordResetRequestController implements Route {

  @Inject Gson gson;

  @Override public String handle(Request request, Response response) {
    String accessId = request.params("accessId");
    if(accessId == null || accessId.isEmpty()) throw new BadRequestException("accessId is required");
    
    return "";
  }
}
