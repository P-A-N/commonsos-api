package commonsos.controller.community;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.annotation.ReadOnly;
import commonsos.exception.BadRequestException;
import commonsos.service.CommunityService;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.CommunityNotificationListView;
import commonsos.view.CommunityNotificationView;
import commonsos.view.PagenationView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

@ReadOnly
public class CommunityNotificationListController implements Route {
  
  @Inject Gson gson;
  @Inject CommunityService service;
  
  @Override
  public CommunityNotificationListView handle(Request request, Response response) throws Exception {
    String communityId = request.params("id");
    if (StringUtils.isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");

    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);
    
    List<CommunityNotificationView> notificationList = service.notificationList(Long.parseLong(communityId));
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    CommunityNotificationListView view = new CommunityNotificationListView()
        .setNotificationList(notificationList)
        .setPagenation(pagenationView);
    
    return view;
  }
}
