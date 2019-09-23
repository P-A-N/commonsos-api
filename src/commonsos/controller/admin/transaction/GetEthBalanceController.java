package commonsos.controller.admin.transaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import commonsos.controller.AbstractController;
import spark.Request;
import spark.Response;

public class GetEthBalanceController extends AbstractController {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("balance", new BigDecimal("999"));
    
    return result;
  }
}
