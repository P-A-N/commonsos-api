package commonsos.controller.app.message;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.app.UploadPhotoForAppController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.UploadPhotoCommand;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class MessageThreadPhotoUpdateController extends UploadPhotoForAppController {
  @Inject MessageService service;

  @Override
  protected String handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response) {
    String id = request.params("id");
    if(StringUtils.isEmpty(id)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(id)) throw new BadRequestException("invalid id");

    long threadId = Long.parseLong(id);
    return service.updatePhoto(user, command, threadId);
  }
}
