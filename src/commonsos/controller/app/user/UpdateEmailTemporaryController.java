package commonsos.controller.app.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.controller.command.app.UpdateEmailTemporaryCommand;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class UpdateEmailTemporaryController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected CommonView handleAfterLogin(User user, Request request, Response response) {
    UpdateEmailTemporaryCommand command = gson.fromJson(request.body(), UpdateEmailTemporaryCommand.class);
    if(StringUtils.isEmpty(command.getNewEmailAddress())) throw new BadRequestException("newEmailAddress is required");
    
    command.setUserId(user.getId());
    
    userService.updateEmailTemporary(command);

    return new CommonView();
  }
}
