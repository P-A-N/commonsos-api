package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.controller.command.app.LastViewTimeUpdateCommand;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public class UpdateAdLastViewTimeController extends AfterAppLoginController {

  @Inject UserService service;
  @Inject Gson gson;

  @Override
  public CommonView handleAfterLogin(User user, Request request, Response response) {
    LastViewTimeUpdateCommand command = gson.fromJson(request.body(), LastViewTimeUpdateCommand.class);
    service.updateAdLastViewTime(user, command);
    return new CommonView();
  }
}
