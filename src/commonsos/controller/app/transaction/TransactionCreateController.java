package commonsos.controller.app.transaction;

import static commonsos.annotation.SyncObject.REGIST_TOKEN_TRANSACTION;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.TokenTransactionService;
import commonsos.service.command.TransactionCreateCommand;
import commonsos.view.app.BalanceView;
import spark.Request;
import spark.Response;

@Synchronized(REGIST_TOKEN_TRANSACTION)
public class TransactionCreateController extends AfterAppLoginController {

  @Inject TokenTransactionService service;
  @Inject Gson gson;

  @Override protected BalanceView handleAfterLogin(User user, Request request, Response response) {
    TransactionCreateCommand command = gson.fromJson(request.body(), TransactionCreateCommand.class);
    service.create(user, command);
    
    return service.balance(user, command.getCommunityId());
  }
}
