package commonsos.controller.admin.transaction;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.BadRequestException;
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

@ReadOnly
public class GetTokenBalanceController extends AfterAdminLoginController {

  @Inject TokenTransactionService tokenTransactionService;
  
  @Override
  protected CommunityTokenBalanceView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    String walletTypeString = RequestUtil.getQueryParamString(request, "wallet", true);
    
    WalletType walletType = WalletType.of(walletTypeString);
    if (walletType == null) throw new BadRequestException("invalid wallet");
    
    if (!AdminUtil.isSeeable(admin, communityId)) throw new ForbiddenException();
    
    TokenBalance tokenBalance = tokenTransactionService.getTokenBalanceForAdmin(admin, communityId, walletType);
    return TransactionUtil.communityTokenBalanceView(tokenBalance, walletType);
  }
}
