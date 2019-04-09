package commonsos.controller.community;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.service.CommunityService;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.CommunityListView;
import commonsos.view.CommunityView;
import commonsos.view.PagenationView;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class CommunityListController implements Route {

  @Inject CommunityService service;

  @Override
  public CommunityListView handle(Request request, Response response) {
    String filter = request.queryParams("filter");
    
    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);
    
    List<CommunityView> communityList = service.list(filter);
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    CommunityListView view = new CommunityListView()
        .setCommunityList(communityList)
        .setPagenation(pagenationView);
    
    return view;
  }
}
