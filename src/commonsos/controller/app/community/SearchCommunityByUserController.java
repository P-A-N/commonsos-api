package commonsos.controller.app.community;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AbstractAppController;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityListView;
import spark.Request;
import spark.Response;

public class SearchCommunityByUserController extends AbstractAppController {

  @Inject CommunityService service;

  @Override
  public CommunityListView handleApp(Request request, Response response) {
    String filter = request.queryParams("filter");
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityListView view = service.search(filter, paginationCommand);
    return view;
  }
}
