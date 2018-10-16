package commonsos.controller.transaction;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.util.List;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.exception.BadRequestException;
import commonsos.repository.user.User;
import commonsos.service.transaction.TransactionService;
import commonsos.service.transaction.TransactionView;
import spark.Request;
import spark.Response;

public class TransactionListController extends Controller {

  @Inject private TransactionService service;

  @Override protected List<TransactionView> handle(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    
    return service.transactions(user, parseLong(communityId));
  }
}
