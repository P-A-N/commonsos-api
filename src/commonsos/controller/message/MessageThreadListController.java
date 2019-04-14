package commonsos.controller.message;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.MessageThreadListCommand;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.MessageThreadListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MessageThreadListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected MessageThreadListView handleAfterLogin(User user, Request request, Response response) {
    MessageThreadListCommand command = new MessageThreadListCommand()
        .setMemberFilter(request.queryParams("memberFilter"))
        .setMessageFilter(request.queryParams("messageFilter"));

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    MessageThreadListView view = service.searchThreads(user, command, paginationCommand);
    return view;
  }
}
