package commonsos.controller.ad;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.ad.AdCreateCommand;
import commonsos.service.ad.AdService;
import commonsos.service.ad.AdView;
import spark.Request;
import spark.Response;

public class AdCreateController extends Controller {

  @Inject AdService service;
  @Inject Gson gson;

  @Override public AdView handle(User user, Request request, Response response) {
    AdCreateCommand command = gson.fromJson(request.body(), AdCreateCommand.class);
    return service.create(user, command);
  }
}
