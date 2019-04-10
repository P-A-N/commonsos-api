package commonsos.controller.transaction;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.PaginationView;
import commonsos.view.TransactionListView;
import commonsos.view.TransactionView;
import spark.Request;
import spark.Response;

@ReadOnly
public class TransactionListController extends AfterLoginController {

  @Inject private TransactionService service;

  @Override protected TransactionListView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    List<TransactionView> transactionList = service.transactions(user, parseLong(communityId));
    PaginationView paginationView = PaginationUtil.toView(paginationCommand);
    TransactionListView view = new TransactionListView()
        .setTransactionList(transactionList)
        .setPagination(paginationView);
    
    return view;
  }
}
