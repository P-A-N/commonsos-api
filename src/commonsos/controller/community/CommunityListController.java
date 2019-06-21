package commonsos.controller.community;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.service.CommunityService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityListView;
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
    
    CommunityListView view = service.list(filter, paginationCommand);
    return view;
  }
}
