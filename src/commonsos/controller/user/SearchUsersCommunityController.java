package commonsos.controller.user;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommunityUserView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchUsersCommunityController extends AfterLoginController {

  @Inject UserService service;

  @Override
  protected List<CommunityUserView> handleAfterLogin(User user, Request request, Response response) {
    String filter = request.queryParams("filter");
    return service.searchUsersCommunity(user, filter);
  }
}
