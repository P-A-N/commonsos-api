package commonsos.controller.message;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.ImmutableMap;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import spark.Request;
import spark.Response;

@ReadOnly
public class MessageThreadUnreadCountController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected Map<String, Object> handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");
    
    return ImmutableMap.of("count", service.unreadMessageThreadCount(user, parseLong(communityId)));
  }
}
