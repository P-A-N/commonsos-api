package commonsos.controller.message;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import java.util.List;

public class MessageThreadListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected List<MessageThreadView> handle(User user, Request request, Response response) {
    return service.threads(user);
  }
}
