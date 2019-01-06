package commonsos.controller.user;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.CommunityService;
import commonsos.view.CommunityView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchUsersCommunityController extends AfterLoginController {

  @Inject CommunityService service;

  @Override
  protected List<CommunityView> handleAfterLogin(User user, Request request, Response response) {
    String filter = request.queryParams("filter");
    return service.usersCommunitylist(user, filter);
  }
}
