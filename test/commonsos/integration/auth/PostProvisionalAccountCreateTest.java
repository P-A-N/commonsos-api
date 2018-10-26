package commonsos.integration.auth;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class PostProvisionalAccountCreateTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  private User admin;
  
  @Before
  public void createUser() {
    admin = create(new User().setUsername("admin"));
    community1 =  create(new Community().setName("community1").setAdminUser(admin));
    community2 =  create(new Community().setName("community2").setAdminUser(admin));
    community3 =  create(new Community().setName("community3").setAdminUser(admin));
  }
  
  @Test
  public void provisionalAccountCreate_multipleCommunity() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "user");
    requestParam.put("password", "password");
    requestParam.put("firstName", "firstName");
    requestParam.put("lastName", "lastName");
    requestParam.put("description", "description");
    requestParam.put("location", "location");
    requestParam.put("emailAddress", "test@test.com");
    requestParam.put("waitUntilCompleted", false);
    List<Long> communityList = new ArrayList<Long>(Arrays.asList(community1.getId(), community2.getId()));
    requestParam.put("communityList", communityList);

    given()
      .body(gson.toJson(requestParam))
      .when().post("/create-account")
      .then().statusCode(200);
    
//    given()
//      .body(gson.toJson(requestParam))
//      .when().post("/create-account")
//      .then().statusCode(200)
//      .body("username", equalTo("user"))
//      .body("communityList.id", contains(
//          community1.getId().intValue(),
//          community2.getId().intValue()))
//      .body("communityList.name", contains(
//          "community1",
//          "community2"))
//      .body("communityList.adminUserId", contains(
//          admin.getId().intValue(),
//          admin.getId().intValue()))
//      .body("balanceList.communityId", contains(
//          community1.getId().intValue(),
//          community2.getId().intValue()));
  }
  
  @Test
  public void provisionalACreate_singleCommunity() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "user");
    requestParam.put("password", "password");
    requestParam.put("firstName", "firstName");
    requestParam.put("lastName", "lastName");
    requestParam.put("description", "description");
    requestParam.put("location", "location");
    requestParam.put("emailAddress", "test@test.com");
    requestParam.put("waitUntilCompleted", false);
    List<Long> communityList = new ArrayList<Long>(Arrays.asList(community1.getId()));
    requestParam.put("communityList", communityList);

    given()
      .body(gson.toJson(requestParam))
      .when().post("/create-account")
      .then().statusCode(200);
    
//    given()
//      .body(gson.toJson(requestParam))
//      .when().post("/create-account")
//      .then().statusCode(200)
//      .body("username", equalTo("user"));
  }
}
