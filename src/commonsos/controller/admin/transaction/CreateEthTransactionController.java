package commonsos.controller.admin.transaction;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.admin.CreateEthTransactionCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.EthTransactionService;
import spark.Request;
import spark.Response;

public class CreateEthTransactionController extends AfterAdminLoginController {

  @Inject private Gson gson;
  @Inject private EthTransactionService ethTransactionService;
  
  @Override
  public Object handleAfterLogin(Admin admin, Request request, Response response) {
    CreateEthTransactionCommand command = gson.fromJson(request.body(), CreateEthTransactionCommand.class);
    ethTransactionService.createEthTransaction(admin, command);

    return "";
  }
}
