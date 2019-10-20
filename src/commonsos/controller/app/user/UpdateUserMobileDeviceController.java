package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UpdateMobileDeviceCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public class UpdateUserMobileDeviceController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected CommonView handleAfterLogin(User user, Request request, Response response) {
    UpdateMobileDeviceCommand command = gson.fromJson(request.body(), UpdateMobileDeviceCommand.class);
    userService.updateMobileDevice(user, command);
    return new CommonView();
  }
}
