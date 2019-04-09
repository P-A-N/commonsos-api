package commonsos.controller.user;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.CommunityUserListView;
import commonsos.view.CommunityUserView;
import commonsos.view.PagenationView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchUsersCommunityController extends AfterLoginController {

  @Inject UserService service;

  @Override
  protected CommunityUserListView handleAfterLogin(User user, Request request, Response response) {
    String filter = request.queryParams("filter");

    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);
    
    List<CommunityUserView> communityList = service.searchUsersCommunity(user, filter);
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    CommunityUserListView view = new CommunityUserListView()
        .setCommunityList(communityList)
        .setPagenation(pagenationView);
    
    return view;
  }
}
