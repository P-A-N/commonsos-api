package commonsos.controller.wordpress.community;

import javax.inject.Inject;

import commonsos.controller.command.PaginationCommand;
import commonsos.controller.wordpress.AbstractWordpressController;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.app.CommunityNotificationListView;
import spark.Request;
import spark.Response;

public class WPCommunityNotificationListController extends AbstractWordpressController {
  
  @Inject CommunityService service;
  
  @Override
  public CommunityNotificationListView handleWordpress(Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityNotificationListView view = service.notificationList(communityId, paginationCommand);
    return view;
  }
}
