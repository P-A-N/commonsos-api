package commonsos.controller.app.user;

import com.google.gson.Gson;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.MobileDeviceUpdateCommand;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class UserMobileDeviceUpdateController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override protected Object handleAfterLogin(User user, Request request, Response response) {
    MobileDeviceUpdateCommand command = gson.fromJson(request.body(), MobileDeviceUpdateCommand.class);
    userService.updateMobileDevice(user, command);
    return "";
  }
}
