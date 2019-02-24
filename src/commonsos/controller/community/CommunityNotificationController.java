package commonsos.controller.community;

import static commonsos.annotation.SyncObject.REGIST_COMMUNITY_NOTIFICATION;
import static java.lang.Long.parseLong;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.CommunityService;
import commonsos.service.command.CommunityNotificationCommand;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

@Synchronized(REGIST_COMMUNITY_NOTIFICATION)
public class CommunityNotificationController extends AfterLoginController {
  
  private static String[] DATE_FORMAT = {
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd HH:mm",
      "yyyy-MM-dd HH",
      "yyyy-MM-dd"
      };

  @Inject Gson gson;
  @Inject CommunityService service;

  @Override protected Object handleAfterLogin(User user, Request request, Response response) {
    CommunityNotificationCommand command = gson.fromJson(request.body(), CommunityNotificationCommand.class);
    
    String communityId = request.params("id");
    if (StringUtils.isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");
    command.setCommunityId(parseLong(communityId));
    
    String wordpressId = request.params("wordpressId");
    if (StringUtils.isEmpty(communityId)) throw new BadRequestException("wordpressId is required");
    command.setWordpressId(wordpressId);
    
    if (StringUtils.isEmpty(command.getUpdatedAt())) throw new BadRequestException("updatedAt is required");
    try {
      Date date = DateUtils.parseDate(command.getUpdatedAt(), DATE_FORMAT);
      command.setUpdatedAtInstant(date.toInstant());
    } catch (ParseException e) {
      throw new BadRequestException(String.format("invalid date format. updatedAt=", command.getUpdatedAt()), e);
    }
    
    service.updateNotificationUpdateAt(user, command);
    return "";
  }
}
