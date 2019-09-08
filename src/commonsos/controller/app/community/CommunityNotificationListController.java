package commonsos.controller.app.community;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AbstractAppController;
import commonsos.controller.command.PaginationCommand;
import commonsos.exception.BadRequestException;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.view.app.CommunityNotificationListView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

@ReadOnly
public class CommunityNotificationListController extends AbstractAppController {
  
  @Inject Gson gson;
  @Inject CommunityService service;
  
  @Override
  public CommunityNotificationListView handleApp(Request request, Response response) {
    String communityId = request.params("id");
    if (StringUtils.isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityNotificationListView view = service.notificationList(Long.parseLong(communityId), paginationCommand);
    return view;
  }
}
