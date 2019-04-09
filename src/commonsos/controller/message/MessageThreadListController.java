package commonsos.controller.message;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.MessageThreadListCommand;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.MessageThreadListView;
import commonsos.view.MessageThreadView;
import commonsos.view.PagenationView;
import spark.Request;
import spark.Response;

@ReadOnly
public class MessageThreadListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected MessageThreadListView handleAfterLogin(User user, Request request, Response response) {
    MessageThreadListCommand command = new MessageThreadListCommand()
        .setMemberFilter(request.queryParams("memberFilter"))
        .setMessageFilter(request.queryParams("messageFilter"));

    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);
    
    List<MessageThreadView> messageThreadList = service.searchThreads(user, command);
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    MessageThreadListView view = new MessageThreadListView()
        .setMessageThreadList(messageThreadList)
        .setPagenation(pagenationView);
    
    return view;
  }
}
