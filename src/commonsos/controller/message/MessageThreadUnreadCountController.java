package commonsos.controller.message;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import spark.Request;
import spark.Response;

@ReadOnly
public class MessageThreadUnreadCountController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected Map<String, Object> handleAfterLogin(User user, Request request, Response response) {
    return ImmutableMap.of("count", service.unreadMessageThreadCount(user));
  }
}
