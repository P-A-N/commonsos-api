package commonsos.controller.app.message;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.util.PaginationUtil;
import commonsos.view.app.MessageListView;
import spark.Request;
import spark.Response;

public class MessageListController extends AfterAppLoginController {

  @Inject MessageService service;

  @Override
  protected MessageListView handleAfterLogin(User user, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    MessageListView view = service.messages(user, parseLong(request.params("id")), paginationCommand);
    return view;
  }
}
