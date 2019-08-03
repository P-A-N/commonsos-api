package commonsos.controller.admin.admin;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;

public class CreateAdminCompleteController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", 1);
    result.put("adminname", "鈴木太郎");
    result.put("communityId", 1);
    result.put("roleId", 1);
    result.put("rolename", "コミュニティ管理者");
    result.put("emailAddress", "suzuki@admin.test");
    result.put("telNo", "00088884444");
    result.put("department", "遠野市役所");
    result.put("photoUrl", "https://commonsos-test.s3.amazonaws.com/2f63ed4c-3ff0-46cf-8358-eb91efcbe9c0");
    result.put("loggedinAt", Instant.now().minusSeconds(600));
    result.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));
    
    return result;
  }
}
