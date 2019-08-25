package commonsos.controller.app.transaction;

import static commonsos.annotation.SyncObject.REGIST_TOKEN_TRANSACTION;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.TokenTransactionService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.TokenBalance;
import commonsos.service.command.TransactionCreateCommand;
import commonsos.util.TransactionUtil;
import commonsos.view.UserTokenBalanceView;
import spark.Request;
import spark.Response;

@Synchronized(REGIST_TOKEN_TRANSACTION)
public class TransactionCreateController extends AfterAppLoginController {

  @Inject Gson gson;
  @Inject TokenTransactionService tokenTransactionService;
  @Inject BlockchainService blockchainService;

  @Override protected UserTokenBalanceView handleAfterLogin(User user, Request request, Response response) {
    TransactionCreateCommand command = gson.fromJson(request.body(), TransactionCreateCommand.class);
    tokenTransactionService.create(user, command);
    TokenBalance tokenBalance = blockchainService.getTokenBalance(user, command.getCommunityId());
    
    return TransactionUtil.userTokenBalanceView(tokenBalance);
  }
}
