package commonsos.controller.admin.transaction;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.command.admin.SearchEthBalanceHistoriesCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.EthBalanceHistoryService;
import commonsos.util.PaginationUtil;
import commonsos.util.RequestUtil;
import commonsos.view.EthBalanceListView;
import spark.Request;
import spark.Response;

public class SearchEthBalanceHistoriesController extends AfterAdminLoginController {

  @Inject private EthBalanceHistoryService service;
  
  @Override
  protected EthBalanceListView handleAfterLogin(Admin admin, Request request, Response response) {
    SearchEthBalanceHistoriesCommand command = new SearchEthBalanceHistoriesCommand();
    command.setCommunityId(RequestUtil.getQueryParamLong(request, "communityId", true));
    command.setFrom(RequestUtil.getQueryParamLocalDate(request, "from", false));
    command.setTo(RequestUtil.getQueryParamLocalDate(request, "to", false));

    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    EthBalanceListView view = service.searchEthBalanceHistory(admin, command, paginationCommand);
    return view;
  }
}
