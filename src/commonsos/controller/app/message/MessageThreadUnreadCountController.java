package commonsos.controller.app.message;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.CountView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MessageThreadUnreadCountController extends AfterAppLoginController {

  @Inject MessageService service;

  @Override
  protected CountView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");
    
    int count = service.unreadMessageThreadCount(user, parseLong(communityId));
    return new CountView().setCount(count);
  }
}
