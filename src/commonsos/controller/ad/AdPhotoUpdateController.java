package commonsos.controller.ad;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.AdPhotoUpdateCommand;
import spark.Request;
import spark.Response;

public class AdPhotoUpdateController extends AfterLoginController {
  @Inject AdService service;

  @Override protected String handle(User user, Request request, Response response) {
    long adId = Long.parseLong(request.params("id"));
    return service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(adId).setPhoto(image(request)));
  }
}
