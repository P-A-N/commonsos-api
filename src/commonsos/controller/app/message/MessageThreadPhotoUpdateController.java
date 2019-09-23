package commonsos.controller.app.message;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.command.app.UploadPhotoCommand;
import commonsos.controller.app.UploadPhotoForAppController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.UrlView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class MessageThreadPhotoUpdateController extends UploadPhotoForAppController {
  @Inject MessageService service;

  @Override
  protected UrlView handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response) {
    String id = request.params("id");
    if(StringUtils.isEmpty(id)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(id)) throw new BadRequestException("invalid id");

    long threadId = Long.parseLong(id);
    String url = service.updatePhoto(user, command, threadId);
    return new UrlView().setUrl(url);
  }
}
