package commonsos.controller.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.exception.BadRequestException;
import spark.Request;
import spark.Response;
import spark.Route;

public class EmailUpdateCompleteController implements Route {

  @Inject Gson gson;

  @Override public String handle(Request request, Response response) {
    String id = request.params("id");
    String accessId = request.params("accessId");
    if(id == null || id.isEmpty()) throw new BadRequestException("id is required");
    if(accessId == null || accessId.isEmpty()) throw new BadRequestException("accessId is required");
    
    return "";
  }
}
