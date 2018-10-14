package commonsos.controller.transaction;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.transaction.TransactionService;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class BalanceController extends Controller {

  @Inject TransactionService service;

  @Override protected Object handle(User user, Request request, Response response) {
    return service.balance(user);
  }
}
