package commonsos.controller.app.community;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AbstractAppController;
import commonsos.controller.command.PaginationCommand;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.view.app.CommunityListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class CommunityListController extends AbstractAppController {

  @Inject CommunityService service;

  @Override
  public CommunityListView handleApp(Request request, Response response) {
    String filter = request.queryParams("filter");
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityListView view = service.list(filter, paginationCommand);
    return view;
  }
}
