package commonsos.controller.admin.transaction;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.WalletType;
import commonsos.service.TokenTransactionService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.TransactionListView;
import spark.Request;
import spark.Response;

public class SearchCommunityTokenTransactionsController extends AfterAdminLoginController {

  @Inject private TokenTransactionService service;

  @Override
  protected TransactionListView handleAfterLogin(Admin admin, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    WalletType walletType = RequestUtil.getQueryParamWallet(request, "wallet", true);
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    TransactionListView view = service.searchCommunityTranByAdmin(admin, communityId, walletType, paginationCommand);
    return view;
  }
}
