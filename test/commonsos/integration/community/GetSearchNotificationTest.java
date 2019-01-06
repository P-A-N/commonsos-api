package commonsos.integration.community;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Notification;
import commonsos.repository.entity.User;

public class GetSearchNotificationTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User user;
  private Notification notification1;
  private Notification notification2;
  private Notification notification3;
  private String sessionId;
  
  @Before
  public void createUser() {
    community1 = create(new Community().setName("community1"));
    community2 = create(new Community().setName("community2"));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com"));
    
    notification1 = create(new Notification().setCommunityId(community1.getId()).setTitle("notification1").setUrl("http://test.com/path").setCreatedAt(Instant.now()));
    notification2 = create(new Notification().setCommunityId(community1.getId()).setTitle("notification2").setUrl("http://test.com/path").setCreatedAt(Instant.now()));
    notification3 = create(new Notification().setCommunityId(community2.getId()).setTitle("notification3").setUrl("http://test.com/path").setCreatedAt(Instant.now()));

    sessionId = login("user", "password");
  }
  
  @Test
  public void searchNotification() throws Exception {
    // search community1
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/communities/{id}/notification", community1.getId())
      .then().statusCode(200)
      .body("id", iterableWithSize(2))
      .body("id", contains(notification2.getId().intValue(), notification1.getId().intValue()))
      .body("title", contains("notification2", "notification1"));

    // search community2
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/communities/{id}/notification", community2.getId())
      .then().statusCode(200)
      .body("id", iterableWithSize(1))
      .body("id", contains(notification3.getId().intValue()))
      .body("title", contains("notification3"));
  }
}
