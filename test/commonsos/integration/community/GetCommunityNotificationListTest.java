package commonsos.integration.community;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityNotification;

public class GetCommunityNotificationListTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  
  @BeforeEach
  public void setup() {
    community1 =  create(new Community().setName("community1"));
    community2 =  create(new Community().setName("community2"));
    create(new CommunityNotification().setCommunityId(community1.getId()).setWordpressId("notification1_1").setUpdatedAt(Instant.parse("2019-01-01T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community1.getId()).setWordpressId("notification1_2").setUpdatedAt(Instant.parse("2019-01-02T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community1.getId()).setWordpressId("notification1_3").setUpdatedAt(Instant.parse("2019-01-03T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community2.getId()).setWordpressId("notification2_1").setUpdatedAt(Instant.parse("2019-02-01T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community2.getId()).setWordpressId("notification2_2").setUpdatedAt(Instant.parse("2019-02-02T12:10:10Z")));
  }
  
  @Test
  public void communityNotificationList() {
    // call api
    given()
      .when().get("/communities/{id}/notification", community1.getId())
      .then().statusCode(200)
      .body("wordpressId", contains("notification1_1", "notification1_2", "notification1_3"))
      .body("updatedAt", contains("2019-01-01T12:10:10Z", "2019-01-02T12:10:10Z", "2019-01-03T12:10:10Z"));

    // call api
    given()
      .when().get("/communities/{id}/notification", community2.getId())
      .then().statusCode(200)
      .body("wordpressId", contains("notification2_1", "notification2_2"))
      .body("updatedAt", contains("2019-02-01T12:10:10Z", "2019-02-02T12:10:10Z"));
  }
}
