package commonsos.controller.user;

import java.util.Map;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import spark.Request;
import spark.Response;

public class ProvisionalEmailUpdateController extends AfterLoginController {

  @Inject Gson gson;

  @Override protected String handle(User user, Request request, Response response) {
    Map<String, String> map = gson.fromJson(request.body(), Map.class);
    if(map == null || !map.containsKey("newEmailAddress")) throw new BadRequestException("newEmailAddress is required");
    
    return "";
  }
}
