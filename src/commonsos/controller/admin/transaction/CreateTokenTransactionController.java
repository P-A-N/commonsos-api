package commonsos.controller.admin.transaction;

import static commonsos.annotation.SyncObject.REGIST_TOKEN_TRANSACTION;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.command.admin.CreateTokenTransactionFromAdminCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.TokenTransactionService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.TokenBalance;
import commonsos.util.TokenTransactionUtil;
import commonsos.view.CommunityTokenBalanceView;
import spark.Request;
import spark.Response;

@Synchronized(REGIST_TOKEN_TRANSACTION)
public class CreateTokenTransactionController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject TokenTransactionService tokenTransactionService;
  @Inject BlockchainService blockchainService;
  
  @Override
  protected CommunityTokenBalanceView handleAfterLogin(Admin admin, Request request, Response response) {
    CreateTokenTransactionFromAdminCommand command = gson.fromJson(request.body(), CreateTokenTransactionFromAdminCommand.class);
    tokenTransactionService.create(admin, command);
    TokenBalance tokenBalance = blockchainService.getTokenBalance(command.getCommunityId(), command.getWallet());
    
    return TokenTransactionUtil.communityTokenBalanceView(tokenBalance, command.getWallet());
  }
}
