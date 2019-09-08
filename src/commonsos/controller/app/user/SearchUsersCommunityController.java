package commonsos.controller.app.user;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.PaginationUtil;
import commonsos.view.app.CommunityUserListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchUsersCommunityController extends AfterAppLoginController {

  @Inject UserService service;

  @Override
  protected CommunityUserListView handleAfterLogin(User user, Request request, Response response) {
    String filter = request.queryParams("filter");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityUserListView view = service.searchUsersCommunity(user, filter, paginationCommand);
    return view;
  }
}
