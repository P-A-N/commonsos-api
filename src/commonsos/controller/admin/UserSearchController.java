package commonsos.controller.admin;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.util.List;

import javax.inject.Inject;

import commonsos.BadRequestException;
import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import commonsos.service.view.UserView;
import spark.Request;
import spark.Response;

public class UserSearchController extends Controller {

  @Inject UserService service;

  @Override public List<UserView> handle(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    String q = request.queryParams("q");
    
    return service.searchUsers(user, parseLong(communityId), q);
  }
}
