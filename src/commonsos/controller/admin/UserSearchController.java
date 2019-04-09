package commonsos.controller.admin;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.PagenationView;
import commonsos.view.UserListView;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;

@ReadOnly
public class UserSearchController extends AfterLoginController {

  @Inject UserService service;

  @Override public UserListView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    String q = request.queryParams("q");
    
    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);

    List<UserView> userList = service.searchUsers(user, parseLong(communityId), q);
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    UserListView view = new UserListView()
        .setUserList(userList)
        .setPagenation(pagenationView);
    return view;
  }
}
