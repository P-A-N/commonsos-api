package commonsos.controller.app.message;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.command.PaginationCommand;
import commonsos.command.app.MessageThreadListCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.util.PaginationUtil;
import commonsos.view.app.MessageThreadListView;
import spark.Request;
import spark.Response;

public class MessageThreadListController extends AfterAppLoginController {

  @Inject MessageService service;

  @Override
  public MessageThreadListView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");
    
    MessageThreadListCommand command = new MessageThreadListCommand()
        .setCommunityId(parseLong(communityId))
        .setMemberFilter(request.queryParams("memberFilter"))
        .setMessageFilter(request.queryParams("messageFilter"));

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    MessageThreadListView view = service.searchThreads(user, command, paginationCommand);
    return view;
  }
}
