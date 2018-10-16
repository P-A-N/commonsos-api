package commonsos.controller.transaction;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.exception.BadRequestException;
import commonsos.repository.user.User;
import commonsos.service.transaction.BalanceView;
import commonsos.service.transaction.TransactionService;
import spark.Request;
import spark.Response;

public class BalanceController extends Controller {

  @Inject TransactionService service;

  @Override protected BalanceView handle(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    
    return service.balance(user, parseLong(communityId));
  }
}
