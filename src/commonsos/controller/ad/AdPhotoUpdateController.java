package commonsos.controller.ad;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.ad.AdPhotoUpdateCommand;
import commonsos.repository.user.User;
import commonsos.service.ad.AdService;
import spark.Request;
import spark.Response;

public class AdPhotoUpdateController extends AfterLoginController {
  @Inject AdService service;

  @Override protected String handle(User user, Request request, Response response) {
    long adId = Long.parseLong(request.params("id"));
    return service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(adId).setPhoto(image(request)));
  }
}
