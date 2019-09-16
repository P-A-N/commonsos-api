package commonsos.controller.wordpress.community;

import static commonsos.annotation.SyncObject.REGIST_COMMUNITY_NOTIFICATION;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.command.app.CommunityNotificationCommand;
import commonsos.controller.wordpress.AbstractWordpressController;
import commonsos.exception.BadRequestException;
import commonsos.service.CommunityService;
import commonsos.util.RequestUtil;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

@Synchronized(REGIST_COMMUNITY_NOTIFICATION)
public class WPCommunityNotificationController extends AbstractWordpressController {
  
  private static String[] DATE_FORMAT = {
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd HH:mm",
      "yyyy-MM-dd HH",
      "yyyy-MM-dd"
      };

  @Inject Gson gson;
  @Inject CommunityService service;

  @Override
  public CommonView handleWordpress(Request request, Response response) {
    CommunityNotificationCommand command = gson.fromJson(request.body(), CommunityNotificationCommand.class);
    command.setCommunityId(RequestUtil.getPathParamLong(request, "id"));
    command.setWordpressId(RequestUtil.getPathParamString(request, "wordpressId"));
    
    if (StringUtils.isEmpty(command.getUpdatedAt())) throw new BadRequestException("updatedAt is required");
    try {
      Date date = DateUtils.parseDate(command.getUpdatedAt(), DATE_FORMAT);
      command.setUpdatedAtInstant(date.toInstant());
    } catch (ParseException e) {
      throw new BadRequestException(String.format("invalid date format. updatedAt=", command.getUpdatedAt()), e);
    }
    
    service.updateNotificationUpdateAt(command);
    return new CommonView();
  }
}
