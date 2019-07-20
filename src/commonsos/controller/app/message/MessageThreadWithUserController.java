package commonsos.controller.app.message;

import static commonsos.annotation.SyncObject.MESSAGE_THRED_BETWEEN_USER;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.app.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.CreateDirectMessageThreadCommand;
import spark.Request;
import spark.Response;

@Synchronized(MESSAGE_THRED_BETWEEN_USER)
public class MessageThreadWithUserController extends AfterLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected Object handleAfterLogin(User user, Request request, Response response) {
    CreateDirectMessageThreadCommand command = gson.fromJson(request.body(), CreateDirectMessageThreadCommand.class);
    
    String userId = request.params("userId");
    if (isEmpty(userId)) throw new BadRequestException("userId is required");
    if (!NumberUtils.isParsable(userId)) throw new BadRequestException("invalid userId");
    
    command.setOtherUserId(Long.parseLong(userId));
    return service.threadWithUser(user, command);
  }
}