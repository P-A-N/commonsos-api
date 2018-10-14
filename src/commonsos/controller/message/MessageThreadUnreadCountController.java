package commonsos.controller.message;

import com.google.common.collect.ImmutableMap;
import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.message.MessageService;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import java.util.Map;

public class MessageThreadUnreadCountController extends Controller {

  @Inject MessageService service;

  @Override protected Map<String, Object> handle(User user, Request request, Response response) {
    return ImmutableMap.of("count", service.unreadMessageThreadCount(user));
  }
}
