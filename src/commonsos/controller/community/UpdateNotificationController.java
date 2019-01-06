package commonsos.controller.community;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.Notification;
import commonsos.repository.entity.User;
import commonsos.service.NotificationService;
import commonsos.service.command.UpdateNotificationCommand;
import commonsos.util.NotificationUtil;
import commonsos.view.NotificationView;
import spark.Request;
import spark.Response;

public class UpdateNotificationController extends AfterLoginController {

  @Inject Gson gson;
  @Inject NotificationService service;

  @Override
  protected NotificationView handleAfterLogin(User user, Request request, Response response) {
    UpdateNotificationCommand command = gson.fromJson(request.body(), UpdateNotificationCommand.class);
    
    if(!NumberUtils.isParsable(request.params("notificationId"))) throw new BadRequestException("invalid id");
    command.setId(Long.parseLong(request.params("notificationId")));
    
    Notification notification = service.update(user, command);
    
    return NotificationUtil.view(notification);
  }
}
