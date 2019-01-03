package commonsos.controller.auth;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.exception.BadRequestException;
import commonsos.service.UserService;
import commonsos.service.command.PasswordResetCommand;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

public class PasswordResetController implements Route {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override public String handle(Request request, Response response) {
    String accessId = request.params("accessId");
    if(accessId == null || accessId.isEmpty()) throw new BadRequestException("accessId is required");
    
    PasswordResetCommand command = gson.fromJson(request.body(), PasswordResetCommand.class);
    if(StringUtils.isEmpty(command.getNewPassword())) throw new BadRequestException("newPassword is required");

    userService.passwordReset(accessId, command.getNewPassword());
    
    return null;
  }
}
