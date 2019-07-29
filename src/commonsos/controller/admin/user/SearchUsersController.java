package commonsos.controller.admin.user;

import java.math.BigDecimal;
import java.time.Instant;
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
public class SearchUsersController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    List<Object> userList = new ArrayList<>();

    Map<String, Object> user1 = new HashMap<>();
    user1.put("id", 1);
    user1.put("username", "suzuki");
    user1.put("status", "元気");
    user1.put("telNo", "00088884444");
    user1.put("avatarUrl", "https://hogehoge.com/path/to/photo");
    user1.put("emailAddress", "suzuki@admin.test");
    user1.put("loggedinAt", Instant.now().minusSeconds(600));
    user1.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));
    Map<String, Object> community = new HashMap<>();
    community.put("id", 1);
    community.put("name", "遠野");
    community.put("tokenSymbol", "tono");
    community.put("balance", new BigDecimal("1000"));
    List<Object> communityList = new ArrayList<>();
    communityList.add(community);
    communityList.add(community);
    user1.put("communityList", communityList);
    
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    userList.add(user1);
    
    PaginationView pagination = new PaginationView();
    pagination.setPage(0);
    pagination.setSize(10);
    pagination.setSort(SortType.ASC);
    pagination.setLastPage(5);
    

    Map<String, Object> result = new HashMap<>();
    result.put("adminList", userList);
    result.put("pagination", pagination);
    
    return result;
  }
}
