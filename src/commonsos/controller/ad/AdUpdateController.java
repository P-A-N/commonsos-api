package commonsos.controller.ad;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.Controller;
import commonsos.repository.ad.Ad;
import commonsos.repository.user.User;
import commonsos.service.ad.AdService;
import commonsos.service.ad.AdUpdateCommand;
import commonsos.service.ad.AdView;
import spark.Request;
import spark.Response;

public class AdUpdateController extends Controller {

  @Inject AdService adService;
  @Inject Gson gson;

  @Override public AdView handle(User user, Request request, Response response) {
    AdUpdateCommand command = gson.fromJson(request.body(), AdUpdateCommand.class);
    command.setId(parseLong(request.params("id")));
    
    Ad updatedAd = adService.updateAd(user, command);
    return adService.view(updatedAd, user);
  }
}
