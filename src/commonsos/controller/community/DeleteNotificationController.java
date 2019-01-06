package commonsos.controller.community;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.NotificationService;
import spark.Request;
import spark.Response;

public class DeleteNotificationController extends AfterLoginController {

  @Inject Gson gson;
  @Inject NotificationService service;

  @Override
  protected Object handleAfterLogin(User user, Request request, Response response) {
    if(!NumberUtils.isParsable(request.params("notificationId"))) throw new BadRequestException("invalid id");
    
    service.deleteLogically(user, Long.parseLong(request.params("notificationId")));
    
    return null;
  }
}
