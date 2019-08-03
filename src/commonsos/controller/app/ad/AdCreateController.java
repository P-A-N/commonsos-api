package commonsos.controller.app.ad;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.AdCreateCommand;
import commonsos.view.app.AdView;
import spark.Request;
import spark.Response;

public class AdCreateController extends AfterAppLoginController {

  @Inject AdService service;
  @Inject Gson gson;

  @Override public AdView handleAfterLogin(User user, Request request, Response response) {
    AdCreateCommand command = gson.fromJson(request.body(), AdCreateCommand.class);
    return service.create(user, command);
  }
}
