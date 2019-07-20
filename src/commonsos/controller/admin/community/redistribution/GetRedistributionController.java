package commonsos.controller.admin.community.redistribution;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class GetRedistributionController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("redistributionId", 1);
    result.put("isAll", false);
    result.put("userId", 1);
    result.put("username", "suzuki");
    result.put("redistributionRate", new BigDecimal("25"));
    
    return result;
  }
}
