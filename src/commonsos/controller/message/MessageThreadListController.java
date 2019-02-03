package commonsos.controller.message;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.MessageThreadListCommand;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MessageThreadListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected List<MessageThreadView> handleAfterLogin(User user, Request request, Response response) {
    MessageThreadListCommand command = new MessageThreadListCommand()
        .setMemberFilter(request.queryParams("memberFilter"))
        .setMessageFilter(request.queryParams("messageFilter"));
    
    return service.searchThreads(user, command);
  }
}
