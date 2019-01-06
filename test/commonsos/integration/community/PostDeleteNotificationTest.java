package commonsos.integration.community;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Notification;
import commonsos.repository.entity.User;

public class PostDeleteNotificationTest extends IntegrationTest {

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
  public void deleteByAdmin() throws Exception {
    // login
    sessionId = login("admin", "password");
    
    // delete notification
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/communities/{:id}/notification/{:notificationId}/delete", community.getId(), notification.getId())
      .then().statusCode(200);
    
    Notification deleted = emService.get().find(Notification.class, notification.getId());
    assertThat(deleted.isDeleted()).isTrue();
  }
  
  @Test
  public void deleteByUser() throws Exception {
    // login
    sessionId = login("user", "password");
    
    // delete notification
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/communities/{:id}/notification/{:notificationId}/delete", community.getId(), notification.getId())
      .then().statusCode(403);
  }
}
