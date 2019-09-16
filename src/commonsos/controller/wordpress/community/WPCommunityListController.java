package commonsos.controller.wordpress.community;

import javax.inject.Inject;

import commonsos.controller.command.PaginationCommand;
import commonsos.controller.wordpress.AbstractWordpressController;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.view.app.CommunityListView;
import spark.Request;
import spark.Response;

public class WPCommunityListController extends AbstractWordpressController {

  @Inject CommunityService service;

  @Override
  public CommunityListView handleWordpress(Request request, Response response) {
    String filter = request.queryParams("filter");
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityListView view = service.list(filter, paginationCommand);
    return view;
  }
}
