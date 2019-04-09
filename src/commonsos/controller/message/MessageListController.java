package commonsos.controller.message;

import static java.lang.Long.parseLong;

import java.util.List;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.PagenationCommand;
import commonsos.util.PagenationUtil;
import commonsos.view.MessageListView;
import commonsos.view.MessageView;
import commonsos.view.PagenationView;
import spark.Request;
import spark.Response;

public class MessageListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected MessageListView handleAfterLogin(User user, Request request, Response response) {
    PagenationCommand pagenationCommand = PagenationUtil.getCommand(request);
    List<MessageView> messageList = service.messages(user, parseLong(request.params("id")));
    PagenationView pagenationView = PagenationUtil.toView(pagenationCommand);
    MessageListView view = new MessageListView()
        .setMessageList(messageList)
        .setPagenation(pagenationView);
    
    return view;
  }
}
