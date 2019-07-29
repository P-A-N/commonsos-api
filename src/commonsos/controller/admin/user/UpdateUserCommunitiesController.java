package commonsos.controller.admin.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class UpdateUserCommunitiesController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", 1);
    result.put("username", "suzuki");
    result.put("status", "元気");
    result.put("telNo", "00088884444");
    result.put("avatarUrl", "https://hogehoge.com/path/to/photo");
    result.put("emailAddress", "suzuki@admin.test");
    result.put("loggedinAt", Instant.now().minusSeconds(600));
    result.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));

    Map<String, Object> community = new HashMap<>();
    community.put("id", 1);
    community.put("name", "遠野");
    community.put("tokenSymbol", "tono");
    community.put("balance", new BigDecimal("1000"));
    List<Object> communityList = new ArrayList<>();
    communityList.add(community);
    communityList.add(community);

    result.put("communityList", communityList);
    
    return result;
  }
}
