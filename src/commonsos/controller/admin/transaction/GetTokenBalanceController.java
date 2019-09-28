package commonsos.controller.admin.transaction;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.WalletType;
import commonsos.service.TokenTransactionService;
import commonsos.service.blockchain.TokenBalance;
import commonsos.util.AdminUtil;
import commonsos.util.RequestUtil;
import commonsos.util.TransactionUtil;
import commonsos.view.CommunityTokenBalanceView;
import spark.Request;
import spark.Response;

public class GetTokenBalanceController extends AfterAdminLoginController {

  @Inject TokenTransactionService tokenTransactionService;
  
  @Override
  protected CommunityTokenBalanceView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    WalletType walletType = RequestUtil.getQueryParamWallet(request, "wallet", true);
    
    if (!AdminUtil.isSeeableCommunity(admin, communityId)) throw new ForbiddenException();
    
    TokenBalance tokenBalance = tokenTransactionService.getTokenBalanceForAdmin(admin, communityId, walletType);
    return TransactionUtil.communityTokenBalanceView(tokenBalance, walletType);
  }
}
