package commonsos.controller.message;

import commonsos.controller.AfterLoginController;
import commonsos.repository.user.User;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import java.util.List;

import static java.lang.Long.parseLong;

public class MessageListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected List<MessageView> handle(User user, Request request, Response response) {
    return service.messages(user, parseLong(request.params("id")));
  }
}
