package commonsos.controller.app.user;

import javax.inject.Inject;

import commonsos.controller.app.UploadPhotoController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.UploadPhotoCommand;
import spark.Request;
import spark.Response;

public class UserAvatarUpdateController extends UploadPhotoController {

  @Inject UserService userService;

  @Override
  protected String handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response) {
    return userService.updateAvatar(user, command);
  }
}
