package commonsos.controller.app.ad;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.app.UploadPhotoForAppController;
import commonsos.controller.command.app.UploadPhotoCommand;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.UrlView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class AdPhotoUpdateController extends UploadPhotoForAppController {
  @Inject AdService service;

  @Override
  protected UrlView handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response) {
    String adId = request.params("id");
    if(StringUtils.isEmpty(adId)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(adId)) throw new BadRequestException("invalid id");
    
    String url = service.updatePhoto(user, command, Long.parseLong(adId));
    return new UrlView().setUrl(url);
  }
}
