package commonsos.controller.admin.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;

public class SearchEthBalanceHistoriesController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    List<Object> balanceList = new ArrayList<>();

    for (int i = 1; i < 10; i++) {
      Map<String, Object> balance = new HashMap<>();
      balance.put("date", "2019070" + i);
      balance.put("balance", new BigDecimal("1000").subtract(new BigDecimal(i)));
      balanceList.add(balance);
    }
    for (int i = 10; i <= 31; i++) {
      Map<String, Object> balance = new HashMap<>();
      balance.put("date", "201907" + i);
      balance.put("balance", new BigDecimal("1000").subtract(new BigDecimal(i)));
      balanceList.add(balance);
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("balanceList", balanceList);

    return result;
  }
}
