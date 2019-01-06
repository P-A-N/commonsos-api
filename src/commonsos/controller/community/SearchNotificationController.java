package commonsos.controller.community;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.NotificationService;
import commonsos.view.NotificationView;
import spark.Request;
import spark.Response;

@ReadOnly
public class SearchNotificationController extends AfterLoginController {

  @Inject NotificationService service;

  @Override
  protected List<NotificationView> handleAfterLogin(User user, Request request, Response response) {
    if(!NumberUtils.isParsable(request.params("id"))) throw new BadRequestException("invalid id");
    
    return service.search(Long.parseLong(request.params("id")));
  }
}
