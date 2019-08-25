package commonsos.controller.app.transaction;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.TokenBalance;
import commonsos.util.RequestUtil;
import commonsos.util.TransactionUtil;
import commonsos.view.UserTokenBalanceView;
import spark.Request;
import spark.Response;

@ReadOnly
public class BalanceController extends AfterAppLoginController {

  @Inject BlockchainService service;

  @Override
  public UserTokenBalanceView handleAfterLogin(User user, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    TokenBalance tokenBalance = service.getTokenBalance(user, communityId);
    
    return TransactionUtil.userTokenBalanceView(tokenBalance);
  }
}
