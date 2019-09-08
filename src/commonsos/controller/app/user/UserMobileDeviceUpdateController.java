package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.controller.command.app.MobileDeviceUpdateCommand;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public class UserMobileDeviceUpdateController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected CommonView handleAfterLogin(User user, Request request, Response response) {
    MobileDeviceUpdateCommand command = gson.fromJson(request.body(), MobileDeviceUpdateCommand.class);
    userService.updateMobileDevice(user, command);
    return new CommonView();
  }
}
