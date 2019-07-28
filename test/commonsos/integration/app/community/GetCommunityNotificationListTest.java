package commonsos.integration.app.community;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

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
    create(new CommunityNotification().setCommunityId(community1.getId()).setWordpressId("notification1_1").setUpdatedNotificationAt(Instant.parse("2019-01-01T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community1.getId()).setWordpressId("notification1_2").setUpdatedNotificationAt(Instant.parse("2019-01-02T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community1.getId()).setWordpressId("notification1_3").setUpdatedNotificationAt(Instant.parse("2019-01-03T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community2.getId()).setWordpressId("notification2_1").setUpdatedNotificationAt(Instant.parse("2019-02-01T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community2.getId()).setWordpressId("notification2_2").setUpdatedNotificationAt(Instant.parse("2019-02-02T12:10:10Z")));
  }
  
  @Test
  public void communityNotificationList() {
    // call api
    given()
      .when().get("/communities/{id}/notification", community1.getId())
      .then().statusCode(200)
      .body("notificationList.wordpressId", contains("notification1_1", "notification1_2", "notification1_3"))
      .body("notificationList.updatedAt", contains("2019-01-01T12:10:10Z", "2019-01-02T12:10:10Z", "2019-01-03T12:10:10Z"));

    // call api
    given()
      .when().get("/communities/{id}/notification", community2.getId())
      .then().statusCode(200)
      .body("notificationList.wordpressId", contains("notification2_1", "notification2_2"))
      .body("notificationList.updatedAt", contains("2019-02-01T12:10:10Z", "2019-02-02T12:10:10Z"));
  }
  
  @Test
  public void communityNotificationList_pagenation() {
    // prepare
    Community community =  create(new Community().setName("page_community"));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification1").setUpdatedNotificationAt(Instant.parse("2019-01-15T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification2").setUpdatedNotificationAt(Instant.parse("2019-01-14T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification3").setUpdatedNotificationAt(Instant.parse("2019-01-13T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification4").setUpdatedNotificationAt(Instant.parse("2019-01-12T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification5").setUpdatedNotificationAt(Instant.parse("2019-01-11T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification6").setUpdatedNotificationAt(Instant.parse("2019-01-10T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification7").setUpdatedNotificationAt(Instant.parse("2019-01-16T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification8").setUpdatedNotificationAt(Instant.parse("2019-01-17T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification9").setUpdatedNotificationAt(Instant.parse("2019-01-18T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification10").setUpdatedNotificationAt(Instant.parse("2019-01-19T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification11").setUpdatedNotificationAt(Instant.parse("2019-01-20T12:10:10Z")));
    create(new CommunityNotification().setCommunityId(community.getId()).setWordpressId("page_notification12").setUpdatedNotificationAt(Instant.parse("2019-01-21T12:10:10Z")));

    // page 0 size 10 asc
    given()
      .when().get("/communities/{id}/notification?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("notificationList.wordpressId", contains(
          "page_notification1", "page_notification2", "page_notification3", "page_notification4", "page_notification5",
          "page_notification6", "page_notification7", "page_notification8", "page_notification9", "page_notification10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .when().get("/communities/{id}/notification?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("notificationList.wordpressId", contains(
          "page_notification11", "page_notification12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .when().get("/communities/{id}/notification?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("notificationList.wordpressId", contains(
          "page_notification12", "page_notification11", "page_notification10", "page_notification9", "page_notification8",
          "page_notification7", "page_notification6", "page_notification5", "page_notification4", "page_notification3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .when().get("/communities/{id}/notification?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("notificationList.wordpressId", contains(
          "page_notification2", "page_notification1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
