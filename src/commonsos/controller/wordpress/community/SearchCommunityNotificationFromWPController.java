package commonsos.controller.wordpress.community;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.wordpress.AbstractWordpressController;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.CommunityNotificationListView;
import spark.Request;
import spark.Response;

public class SearchCommunityNotificationFromWPController extends AbstractWordpressController {
  
  @Inject CommunityService service;
  
  @Override
  public CommunityNotificationListView handleWordpress(Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityNotificationListView view = service.notificationList(communityId, paginationCommand);
    return view;
  }
}
