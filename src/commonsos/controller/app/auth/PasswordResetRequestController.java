package commonsos.controller.app.auth;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.exception.BadRequestException;
import commonsos.service.UserService;
import commonsos.service.command.PasswordResetRequestCommand;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

public class PasswordResetRequestController implements Route {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override public String handle(Request request, Response response) {
    PasswordResetRequestCommand command = gson.fromJson(request.body(), PasswordResetRequestCommand.class);
    if(StringUtils.isEmpty(command.getEmailAddress())) throw new BadRequestException("emailAddress is required");

    userService.passwordResetRequest(command);
    
    return null;
  }
}
