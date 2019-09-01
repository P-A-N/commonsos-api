package commonsos.controller.app.user;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.annotation.ReadOnly;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

@ReadOnly
public class GetTransactionQrCodeController extends AfterAppLoginController {

  @Inject UserService userService;

  @Override
  protected Object handleAfterLogin(User user, Request request, Response response) {
    Long communityId = RequestUtil.getQueryParamLong(request, "communityId", true);
    String amount = RequestUtil.getQueryParamString(request, "amount", false);

    String url;
    if (amount != null) {
      if (!NumberUtils.isParsable(amount)) throw new BadRequestException("invalid amount");
      url = userService.getQrCodeUrl(user, communityId, new BigDecimal(amount));
    } else {
      url = userService.getQrCodeUrl(user, communityId, null);
    }
    
    Map<String, String> result = new HashMap<>();
    result.put("url", url);
    
    return result;
  }
}
