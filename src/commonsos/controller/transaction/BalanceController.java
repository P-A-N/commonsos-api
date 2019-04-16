package commonsos.controller.transaction;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.view.BalanceView;
import spark.Request;
import spark.Response;

@ReadOnly
public class BalanceController extends AfterLoginController {

  @Inject TransactionService service;

  @Override
  public BalanceView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");
    
    return service.balance(user, parseLong(communityId));
  }
}
