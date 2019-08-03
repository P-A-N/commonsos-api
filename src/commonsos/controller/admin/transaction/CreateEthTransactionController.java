package commonsos.controller.admin.transaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;

public class CreateEthTransactionController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("balance", new BigDecimal("999"));
    
    return result;
  }
}
