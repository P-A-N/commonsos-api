package commonsos.controller.user;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityUserListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchUsersCommunityController extends AfterLoginController {

  @Inject UserService service;

  @Override
  protected CommunityUserListView handleAfterLogin(User user, Request request, Response response) {
    String filter = request.queryParams("filter");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityUserListView view = service.searchUsersCommunity(user, filter, paginationCommand);
    return view;
  }
}
