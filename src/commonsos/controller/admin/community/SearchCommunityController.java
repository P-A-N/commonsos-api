package commonsos.controller.admin.community;

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
public class SearchCommunityController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    List<Object> communityList = new ArrayList<>();

    Map<String, Object> community1 = new HashMap<>();
    community1.put("communityId", 1);
    community1.put("communityName", "テストコミュニティ");
    community1.put("tokenName", "テストコイン");
    community1.put("tokenSymbol", "ts");
    community1.put("transactionFee", new BigDecimal("1.5"));
    community1.put("description", "コミュニティの説明");
    community1.put("status", "PUBLIC");
    community1.put("adminPageUrl", "https://hogehoge.com/path/to/login");
    community1.put("totalSupply", new BigDecimal("1000000000000"));
    community1.put("totalMember", new BigDecimal("10"));
    community1.put("photoUrl", "https://commonsos-test.s3.amazonaws.com/2f63ed4c-3ff0-46cf-8358-eb91efcbe9c0");
    community1.put("coverPhotoUrl", "https://commonsos-test.s3.amazonaws.com/2f63ed4c-3ff0-46cf-8358-eb91efcbe9c0");
    
    List<Object> adminList = new ArrayList<>();
    Map<String, Object> admin = new HashMap<>();
    admin.put("adminId", 1);
    admin.put("adminname", "AKIRA");
    adminList.add(admin);
    adminList.add(admin);
    community1.put("adminList", adminList);
    
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    communityList.add(community1);
    
    PaginationView pagination = new PaginationView();
    pagination.setPage(0);
    pagination.setSize(10);
    pagination.setSort(SortType.ASC);
    pagination.setLastPage(5);
    
    Map<String, Object> result = new HashMap<>();
    result.put("communityList", communityList);
    result.put("pagination", pagination);
    
    return result;
  }
}
