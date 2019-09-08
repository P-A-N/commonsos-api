package commonsos.controller.app.auth;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AbstractAppController;
import commonsos.controller.command.app.PasswordResetRequestCommand;
import commonsos.exception.BadRequestException;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class PasswordResetRequestController extends AbstractAppController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  public CommonView handleApp(Request request, Response response) {
    PasswordResetRequestCommand command = gson.fromJson(request.body(), PasswordResetRequestCommand.class);
    if(StringUtils.isEmpty(command.getEmailAddress())) throw new BadRequestException("emailAddress is required");

    userService.passwordResetRequest(command);
    
    return new CommonView();
  }
}
