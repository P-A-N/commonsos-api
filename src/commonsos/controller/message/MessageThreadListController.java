package commonsos.controller.message;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.MessageThreadListCommand;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.MessageThreadListView;
import commonsos.view.MessageThreadView;
import commonsos.view.PaginationView;
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
    
    List<MessageThreadView> messageThreadList = service.searchThreads(user, command);
    PaginationView paginationView = PaginationUtil.toView(paginationCommand);
    MessageThreadListView view = new MessageThreadListView()
        .setMessageThreadList(messageThreadList)
        .setPagination(paginationView);
    
    return view;
  }
}
