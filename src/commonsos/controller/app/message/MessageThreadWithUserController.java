package commonsos.controller.app.message;

import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.controller.command.app.CreateDirectMessageThreadCommand;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.app.MessageThreadView;
import spark.Request;
import spark.Response;

public class MessageThreadWithUserController extends AfterAppLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override
  protected MessageThreadView handleAfterLogin(User user, Request request, Response response) {
    CreateDirectMessageThreadCommand command = gson.fromJson(request.body(), CreateDirectMessageThreadCommand.class);
    
    String userId = request.params("userId");
    if (isEmpty(userId)) throw new BadRequestException("userId is required");
    if (!NumberUtils.isParsable(userId)) throw new BadRequestException("invalid userId");
    
    command.setOtherUserId(Long.parseLong(userId));
    return service.threadWithUser(user, command);
  }
}