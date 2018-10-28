package commonsos.controller.auth;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.exception.BadRequestException;
import commonsos.service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

public class PasswordResetRequestCheckController implements Route {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override public String handle(Request request, Response response) {
    String accessId = request.params("accessId");
    if(StringUtils.isEmpty(accessId)) throw new BadRequestException("accessId is required");
    
    userService.passwordResetRequestCheck(accessId);
    
    return null;
  }
}
