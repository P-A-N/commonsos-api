package commonsos.integration.community;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Notification;
import commonsos.repository.entity.User;

public class PostUpdateNotificationTest extends IntegrationTest {

  private Community community;
  private User admin;
  private User user;
  private Notification notification;
  private String sessionId;
  
  @Before
  public void createUser() {
    community = create(new Community().setName("community"));
    admin = create(new User().setUsername("admin").setPasswordHash(hash("password")).setEmailAddress("admin@test.com").setCommunityList(asList(community)));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com").setCommunityList(asList(community)));
    update(community.setAdminUser(admin));
    
    notification = create(new Notification()
        .setCommunityId(community.getId())
        .setTitle("hoge")
        .setUrl("http://hogehoge.com/path")
        .setCreatedAt(Instant.now()));
  }
  
  @Test
  public void updateByAdmin() throws Exception {
    // login
    sessionId = login("admin", "password");
    
    // update notification
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("title", "hogehoge");
    requestParam.put("url", "http://test.com/path");

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{:id}/notification/{:notificationId}", community.getId(), notification.getId())
      .then().statusCode(200)
      .body("id", equalTo(notification.getId().intValue()))
      .body("title", equalTo("hogehoge"))
      .body("url", equalTo("http://test.com/path"))
      .body("createdAt", equalTo(notification.getCreatedAt().toString()));
  }
  
  @Test
  public void updateByUser() throws Exception {
    // login
    sessionId = login("user", "password");
    
    // update notification
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("title", "hogehoge");
    requestParam.put("url", "http://test.com/path");

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{:id}/notification/{:notificationId}", community.getId(), notification.getId())
      .then().statusCode(403);
  }
}
