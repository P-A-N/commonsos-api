package commonsos.controller.community;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.CommunityService;
import spark.Request;
import spark.Response;

public class CommunityCoverPhotoUpdateController extends AfterLoginController {

  @Inject CommunityService communityService;

  @Override public String handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.params("id");
    if(!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid id");
    
    return communityService.updateCoverPhoto(user, parseLong(communityId), image(request));
  }
}
