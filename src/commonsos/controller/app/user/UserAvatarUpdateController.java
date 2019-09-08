package commonsos.controller.app.user;

import javax.inject.Inject;

import commonsos.controller.app.UploadPhotoForAppController;
import commonsos.controller.command.app.UploadPhotoCommand;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UrlView;
import spark.Request;
import spark.Response;

public class UserAvatarUpdateController extends UploadPhotoForAppController {

  @Inject UserService userService;

  @Override
  protected UrlView handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response) {
    String url = userService.updateAvatar(user, command);
    return new UrlView().setUrl(url);
  }
}
