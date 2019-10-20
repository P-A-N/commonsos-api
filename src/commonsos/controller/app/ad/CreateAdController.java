package commonsos.controller.app.ad;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.CreateAdCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

public class CreateAdController extends AfterAppLoginController {

  @Inject AdService service;
  @Inject Gson gson;

  @Override public AdView handleAfterLogin(User user, Request request, Response response) {
    CreateAdCommand command = gson.fromJson(request.body(), CreateAdCommand.class);
    return service.create(user, command);
  }
}
