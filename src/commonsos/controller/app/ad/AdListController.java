package commonsos.controller.app.ad;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.app.AdListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class AdListController extends AfterLoginController {
  @Inject AdService service;

  @Override
  public AdListView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    AdListView view = service.listFor(user, parseLong(communityId), request.queryParams("filter"), paginationCommand);
    return view;
  }
}
