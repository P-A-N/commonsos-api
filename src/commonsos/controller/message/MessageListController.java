package commonsos.controller.message;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import java.util.List;

import static java.lang.Long.parseLong;

public class MessageListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected List<MessageView> handleAfterLogin(User user, Request request, Response response) {
    return service.messages(user, parseLong(request.params("id")));
  }
}
