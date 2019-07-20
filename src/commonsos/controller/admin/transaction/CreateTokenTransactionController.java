package commonsos.controller.admin.transaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class CreateTokenTransactionController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("communityId", 1);
    result.put("wallet", "MAIN");
    result.put("tokenSymbol", "tono");
    result.put("balance", new BigDecimal("999"));
    
    return result;
  }
}
