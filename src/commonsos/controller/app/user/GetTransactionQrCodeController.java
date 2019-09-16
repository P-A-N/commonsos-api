package commonsos.controller.app.user;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.RequestUtil;
import commonsos.view.UrlView;
import spark.Request;
import spark.Response;

public class GetTransactionQrCodeController extends AfterAppLoginController {

  @Inject UserService userService;

  @Override
  protected UrlView handleAfterLogin(User user, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    String amount = RequestUtil.getQueryParamString(request, "amount", false);

    String url;
    if (amount != null) {
      if (!NumberUtils.isParsable(amount)) throw new BadRequestException("invalid amount");
      url = userService.getQrCodeUrl(user, communityId, new BigDecimal(amount));
    } else {
      url = userService.getQrCodeUrl(user, communityId, null);
    }
    
    return new UrlView().setUrl(url);
  }
}
