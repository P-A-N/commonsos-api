package commonsos.controller.app.user;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityUserListView;
import spark.Request;
import spark.Response;

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
