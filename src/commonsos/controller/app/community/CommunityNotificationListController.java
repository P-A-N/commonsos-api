package commonsos.controller.app.community;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AbstractAppController;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.CommunityNotificationListView;
import spark.Request;
import spark.Response;

public class CommunityNotificationListController extends AbstractAppController {
  
  @Inject CommunityService service;
  
  @Override
  public CommunityNotificationListView handleApp(Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityNotificationListView view = service.notificationList(communityId, paginationCommand);
    return view;
  }
}
