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
    String amount = RequestUtil.getQueryParamString(request, "amount", false);
    
    Map<String, String> result = new HashMap<>();
    if (amount == null) {
      result.put("url", user.getQrCodeUrl()); 
    } else {
      if (!NumberUtils.isParsable(amount)) throw new BadRequestException("");
      String url = userService.getQrCodeUrl(user, new BigDecimal(amount));
      result.put("url", url); 
    }
    
    return result;
  }
}
