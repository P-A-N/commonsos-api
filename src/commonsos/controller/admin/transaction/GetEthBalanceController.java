package commonsos.controller.admin.transaction;

import javax.inject.Inject;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.EthTransactionService;
import commonsos.service.blockchain.EthBalance;
import commonsos.util.EthTransactionUtil;
import commonsos.util.RequestUtil;
import commonsos.view.EthBalanceView;
import spark.Request;
import spark.Response;

public class GetEthBalanceController extends AfterAdminLoginController {

  @Inject private EthTransactionService ethTransactionService;

  @Override
  public EthBalanceView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    EthBalance balance = ethTransactionService.getEthBalance(admin, communityId);
    return EthTransactionUtil.ethBalanceView(balance);
  }
}
