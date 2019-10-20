package commonsos.controller.app.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UpdateAdCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

public class UpdateAdController extends AfterAppLoginController {

  @Inject AdService adService;
  @Inject Gson gson;

  @Override
  public AdView handleAfterLogin(User user, Request request, Response response) {
    UpdateAdCommand command = gson.fromJson(request.body(), UpdateAdCommand.class);
    command.setId(parseLong(request.params("id")));
    
    Ad updatedAd = adService.updateAd(user, command);
    return adService.view(updatedAd, user);
  }
}
