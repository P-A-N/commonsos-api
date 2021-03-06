package commonsos.controller.transaction;

import static commonsos.annotation.SyncObject.REGIST_TRANSACTION;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.service.command.TransactionCreateCommand;
import spark.Request;
import spark.Response;

@Synchronized(REGIST_TRANSACTION)
public class TransactionCreateController extends AfterLoginController {

  @Inject TransactionService service;
  @Inject Gson gson;

  @Override protected Object handleAfterLogin(User user, Request request, Response response) {
    TransactionCreateCommand command = gson.fromJson(request.body(), TransactionCreateCommand.class);
    service.create(user, command);
    return "";
  }
}
