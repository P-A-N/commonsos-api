package commonsos.controller.admin.admin;

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
public class SearchAdminsController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    List<Object> adminList = new ArrayList<>();

    Map<String, Object> admin1 = new HashMap<>();
    admin1.put("id", 1);
    admin1.put("adminname", "鈴木太郎");
    admin1.put("communityId", 1);
    admin1.put("roleId", 1);
    admin1.put("rolename", "コミュニティ管理者");
    admin1.put("emailAddress", "suzuki@admin.test");
    admin1.put("telNo", "00088884444");
    admin1.put("department", "遠野市役所");
    admin1.put("photoUrl", "https://commonsos-test.s3.amazonaws.com/2f63ed4c-3ff0-46cf-8358-eb91efcbe9c0");
    admin1.put("loggedinAt", Instant.now().minusSeconds(600));
    admin1.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    adminList.add(admin1);
    
    PaginationView pagination = new PaginationView();
    pagination.setPage(0);
    pagination.setSize(10);
    pagination.setSort(SortType.ASC);
    pagination.setLastPage(5);
    

    Map<String, Object> result = new HashMap<>();
    result.put("adminList", adminList);
    result.put("pagination", pagination);
    
    return result;
  }
}
