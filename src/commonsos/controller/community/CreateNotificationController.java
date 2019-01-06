package commonsos.controller.community;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.Notification;
import commonsos.repository.entity.User;
import commonsos.service.NotificationService;
import commonsos.service.command.CreateNotificationCommand;
import commonsos.util.NotificationUtil;
import commonsos.view.NotificationView;
import spark.Request;
import spark.Response;

public class CreateNotificationController extends AfterLoginController {

  @Inject Gson gson;
  @Inject NotificationService service;

  @Override
  protected NotificationView handleAfterLogin(User user, Request request, Response response) {
    CreateNotificationCommand command = gson.fromJson(request.body(), CreateNotificationCommand.class);
    
    if(!NumberUtils.isParsable(request.params("id"))) throw new BadRequestException("invalid id");
    command.setCommunityId(Long.parseLong(request.params("id")));
    
    Notification notification = service.create(user, command);
    
    return NotificationUtil.view(notification);
  }
}
