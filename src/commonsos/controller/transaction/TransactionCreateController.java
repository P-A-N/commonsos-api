package commonsos.controller.transaction;

import com.google.gson.Gson;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.service.command.TransactionCreateCommand;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class TransactionCreateController extends AfterLoginController {

  @Inject TransactionService service;
  @Inject Gson gson;

  @Override protected Object handle(User user, Request request, Response response) {
    TransactionCreateCommand command = gson.fromJson(request.body(), TransactionCreateCommand.class);
    service.create(user, command);
    return "";
  }
}
