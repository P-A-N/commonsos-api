package commonsos.controller.transaction;

import com.google.gson.Gson;
import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.transaction.TransactionCreateCommand;
import commonsos.service.transaction.TransactionService;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class TransactionCreateController extends Controller {

  @Inject TransactionService service;
  @Inject Gson gson;

  @Override protected Object handle(User user, Request request, Response response) {
    TransactionCreateCommand command = gson.fromJson(request.body(), TransactionCreateCommand.class);
    service.create(user, command);
    return "";
  }
}
