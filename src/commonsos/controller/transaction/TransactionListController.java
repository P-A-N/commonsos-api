package commonsos.controller.transaction;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.TransactionListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class TransactionListController extends AfterLoginController {

  @Inject private TransactionService service;

  @Override protected TransactionListView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    TransactionListView view = service.transactions(user, parseLong(communityId), paginationCommand);
    return view;
  }
}
