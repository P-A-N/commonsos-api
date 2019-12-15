package commonsos.controller.admin.user;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.Admin;
import commonsos.service.UserService;
import commonsos.util.RequestUtil;
import commonsos.view.UrlView;
import spark.Request;
import spark.Response;

public class GetTransactionQrCodeByAdminController extends AfterAdminLoginController {

  @Inject UserService userService;
  
  @Override
  protected UrlView handleAfterLogin(Admin admin, Request request, Response response) {
    Long userId = RequestUtil.getPathParamLong(request, "id");
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    String amount = RequestUtil.getQueryParamString(request, "amount", false);

    String url;
    if (amount != null) {
      if (!NumberUtils.isParsable(amount)) throw new BadRequestException("invalid amount");
      url = userService.getQrCodeUrlByAdmin(admin, userId, communityId, new BigDecimal(amount));
    } else {
      url = userService.getQrCodeUrlByAdmin(admin, userId, communityId, null);
    }
    
    return new UrlView().setUrl(url);
  }
}
