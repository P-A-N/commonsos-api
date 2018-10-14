package commonsos.controller.message;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import java.util.List;

public class MessageThreadListController extends Controller {

  @Inject MessageService service;

  @Override protected List<MessageThreadView> handle(User user, Request request, Response response) {
    return service.threads(user);
  }
}
