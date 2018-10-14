package commonsos.controller.admin;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import commonsos.service.view.UserView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import java.util.List;

public class UserSearchController extends Controller {

  @Inject UserService service;

  @Override public List<UserView> handle(User user, Request request, Response response) {
    return service.searchUsers(user, request.queryParams("q"));
  }
}
