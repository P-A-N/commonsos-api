package commonsos.controller.ad;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.UploadPhotoController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.UploadPhotoCommand;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class AdPhotoUpdateController extends UploadPhotoController {
  @Inject AdService service;

  @Override
  protected String handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response) {
    String adId = request.params("id");
    if(StringUtils.isEmpty(adId)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(adId)) throw new BadRequestException("invalid id");
    
    return service.updatePhoto(user, command, Long.parseLong(adId));
  }
}
