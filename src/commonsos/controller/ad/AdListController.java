package commonsos.controller.ad;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.AdListView;
import commonsos.view.AdView;
import commonsos.view.PagenationView;
import spark.Request;
import spark.Response;

@ReadOnly
public class AdListController extends AfterLoginController {
  @Inject AdService service;

  @Override public AdListView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");

    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);
    
    List<AdView> adList = service.listFor(user, parseLong(communityId), request.queryParams("filter"));
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    AdListView view = new AdListView()
        .setAdList(adList)
        .setPagenation(pagenationView);
    
    return view;
  }
}
