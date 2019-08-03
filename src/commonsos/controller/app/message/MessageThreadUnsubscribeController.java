package commonsos.controller.app.message;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class MessageThreadUnsubscribeController extends AfterAppLoginController {
  @Inject MessageService service;

  @Override protected String handleAfterLogin(User user, Request request, Response response) {
    String id = request.params("id");
    if(StringUtils.isEmpty(id)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(id)) throw new BadRequestException("invalid id");
    
    long threadId = Long.parseLong(id);
    service.unsubscribe(user, threadId);
    
    return "";
  }
}
