package commonsos.controller.user;

import com.google.gson.Gson;
import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.user.MobileDeviceUpdateCommand;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class UserMobileDeviceUpdateController extends Controller {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override protected Object handle(User user, Request request, Response response) {
    MobileDeviceUpdateCommand command = gson.fromJson(request.body(), MobileDeviceUpdateCommand.class);
    userService.updateMobileDevice(user, command);
    return "";
  }
}
