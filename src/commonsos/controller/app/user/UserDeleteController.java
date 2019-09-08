package commonsos.controller.app.user;

import javax.inject.Inject;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.DeleteService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public class UserDeleteController extends AfterAppLoginController {

  @Inject DeleteService deleteService;

  @Override
  public CommonView handleAfterLogin(User user, Request request, Response response) {
    deleteService.deleteUser(user);
    return new CommonView();
  }
}
