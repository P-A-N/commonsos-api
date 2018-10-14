package commonsos.controller.ad;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import commonsos.BadRequestException;
import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.ad.AdService;
import spark.Request;
import spark.Response;

public class AdListController extends Controller {
  @Inject AdService service;

  @Override public Object handle(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    
    return service.listFor(user, parseLong(communityId), request.queryParams("filter"));
  }
}
