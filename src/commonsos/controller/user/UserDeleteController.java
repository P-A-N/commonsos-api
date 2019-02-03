package commonsos.controller.user;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.DeleteService;
import spark.Request;
import spark.Response;

public class UserDeleteController extends AfterLoginController {

  @Inject DeleteService deleteService;

  @Override public Object handleAfterLogin(User user, Request request, Response response) {
    deleteService.deleteUser(user);
    return "";
  }
}
