package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.LastViewTimeUpdateCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public class UpdateNotificationLastViewTimeController extends AfterAppLoginController {

  @Inject UserService service;
  @Inject Gson gson;

  @Override
  public CommonView handleAfterLogin(User user, Request request, Response response) {
    LastViewTimeUpdateCommand command = gson.fromJson(request.body(), LastViewTimeUpdateCommand.class);
    service.updateNotificationLastViewTime(user, command);
    return new CommonView();
  }
}
