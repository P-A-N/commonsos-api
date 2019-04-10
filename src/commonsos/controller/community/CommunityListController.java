package commonsos.controller.community;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.service.CommunityService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityListView;
import commonsos.view.CommunityView;
import commonsos.view.PaginationView;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class CommunityListController implements Route {

  @Inject CommunityService service;

  @Override
  public CommunityListView handle(Request request, Response response) {
    String filter = request.queryParams("filter");
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    List<CommunityView> communityList = service.list(filter);
    PaginationView paginationView = PaginationUtil.toView(paginationCommand);
    CommunityListView view = new CommunityListView()
        .setCommunityList(communityList)
        .setPagination(paginationView);
    
    return view;
  }
}
