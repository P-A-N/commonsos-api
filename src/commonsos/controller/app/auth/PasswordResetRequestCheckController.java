package commonsos.controller.app.auth;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AbstractAppController;
import commonsos.exception.BadRequestException;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class PasswordResetRequestCheckController extends AbstractAppController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  public CommonView handleApp(Request request, Response response) {
    String accessId = request.params("accessId");
    if(StringUtils.isEmpty(accessId)) throw new BadRequestException("accessId is required");
    
    userService.passwordResetRequestCheck(accessId);
    
    return new CommonView();
  }
}
