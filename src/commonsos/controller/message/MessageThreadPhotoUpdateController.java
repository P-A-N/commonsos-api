package commonsos.controller.message;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.MessageThreadPhotoUpdateCommand;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class MessageThreadPhotoUpdateController extends AfterLoginController {
  @Inject MessageService service;

  @Override protected String handleAfterLogin(User user, Request request, Response response) {
    String id = request.params("id");
    if(StringUtils.isEmpty(id)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(id)) throw new BadRequestException("invalid id");
    
    long threadId = Long.parseLong(id);
    return service.updatePhoto(user, new MessageThreadPhotoUpdateCommand().setThreadId(threadId).setPhoto(image(request)));
  }
}
