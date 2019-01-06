package commonsos.integration.community;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class PostCreateNotificationTest extends IntegrationTest {

  private Community community;
  private User admin;
  private User user;
  private String sessionId;
  
  @Before
  public void createUser() {
    community = create(new Community().setName("community"));
    admin = create(new User().setUsername("admin").setPasswordHash(hash("password")).setEmailAddress("admin@test.com").setCommunityList(asList(community)));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com").setCommunityList(asList(community)));
    update(community.setAdminUser(admin));
  }
  
  @Test
  public void createByAdmin() throws Exception {
    // login
    sessionId = login("admin", "password");
    
    // create notification
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("title", "hogehoge");
    requestParam.put("url", "http://test.com/path");

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{:id}/notification", community.getId())
      .then().statusCode(200)
      .body("id", notNullValue())
      .body("title", equalTo("hogehoge"))
      .body("url", equalTo("http://test.com/path"))
      .body("createdAt", notNullValue());
  }
  
  @Test
  public void createByUser() throws Exception {
    // login
    sessionId = login("user", "password");
    
    // create notification
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("title", "hogehoge");
    requestParam.put("url", "http://test.com/path");

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{:id}/notification", community.getId())
      .then().statusCode(403);
  }
}
