package commonsos.controller.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.exception.BadRequestException;
import commonsos.service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class UpdateEmailCompleteController implements Route {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override public Object handle(Request request, Response response) {
    String id = request.params("id");
    String accessId = request.params("accessId");
    if(StringUtils.isEmpty(id)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(id)) throw new BadRequestException("invalid id");
    if(StringUtils.isEmpty(accessId)) throw new BadRequestException("accessId is required");
    
    userService.updateEmailComplete(Long.parseLong(id), accessId);
    
    return null;
  }
}
