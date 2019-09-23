package commonsos.controller.admin.community.redistribution;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import commonsos.controller.AbstractController;
import spark.Request;
import spark.Response;

public class UpdateRedistributionController extends AbstractController {

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
