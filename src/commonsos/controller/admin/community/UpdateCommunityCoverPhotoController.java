package commonsos.controller.admin.community;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class UpdateCommunityCoverPhotoController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("communityId", 1);
    result.put("communityName", "テストコミュニティ");
    result.put("tokenName", "テストコイン");
    result.put("tokenSymbol", "ts");
    result.put("transactionFee", new BigDecimal("1.5"));
    result.put("description", "コミュニティの説明");
    result.put("status", "PUBLIC");
    result.put("adminPageUrl", "https://hogehoge.com/path/to/login");
    result.put("totalSupply", new BigDecimal("1000000000000"));
    result.put("totalMember", new BigDecimal("10"));
    result.put("photoUrl", "https://commonsos-test.s3.amazonaws.com/2f63ed4c-3ff0-46cf-8358-eb91efcbe9c0");
    result.put("coverPhotoUrl", "https://commonsos-test.s3.amazonaws.com/2f63ed4c-3ff0-46cf-8358-eb91efcbe9c0");
    
    List<Object> adminList = new ArrayList<>();
    Map<String, Object> admin = new HashMap<>();
    admin.put("adminId", 1);
    admin.put("adminname", "AKIRA");
    adminList.add(admin);
    adminList.add(admin);
    
    result.put("adminList", adminList);
    
    return result;
  }
}
