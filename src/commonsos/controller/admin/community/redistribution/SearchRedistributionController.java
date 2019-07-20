package commonsos.controller.admin.community.redistribution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import commonsos.repository.entity.SortType;
import commonsos.view.PaginationView;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class SearchRedistributionController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    List<Object> redistributionList = new ArrayList<>();

    Map<String, Object> redistribution1 = new HashMap<>();
    redistribution1.put("redistributionId", 1);
    redistribution1.put("isAll", false);
    redistribution1.put("userId", 1);
    redistribution1.put("username", "suzuki");
    redistribution1.put("redistributionRate", new BigDecimal("10"));

    Map<String, Object> redistribution2 = new HashMap<>();
    redistribution2.put("redistributionId", 1);
    redistribution2.put("isAll", true);
    redistribution2.put("redistributionRate", new BigDecimal("10"));

    redistributionList.add(redistribution1);
    redistributionList.add(redistribution2);
    redistributionList.add(redistribution1);
    redistributionList.add(redistribution2);
    redistributionList.add(redistribution1);
    redistributionList.add(redistribution2);
    redistributionList.add(redistribution1);
    redistributionList.add(redistribution2);
    redistributionList.add(redistribution1);
    redistributionList.add(redistribution2);
    
    PaginationView pagination = new PaginationView();
    pagination.setPage(0);
    pagination.setSize(10);
    pagination.setSort(SortType.ASC);
    pagination.setLastPage(5);
    

    Map<String, Object> result = new HashMap<>();
    result.put("redistributionList", redistributionList);
    result.put("pagination", pagination);
    
    return result;
  }
}
