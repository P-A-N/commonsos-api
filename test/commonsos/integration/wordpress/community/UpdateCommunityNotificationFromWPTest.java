package commonsos.integration.wordpress.community;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityNotification;

public class UpdateCommunityNotificationFromWPTest extends IntegrationTest {
  
  private Community community;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setPublishStatus(PUBLIC).setName("community"));
  }
  
  @Test
  public void communityNotification_valid_dateformat() {
    // prepare yyyy-MM-dd HH:mm:ss
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2019-01-01 12:10:10");
    String wordpressId = "wordpress1";
    
    // call api
    given()
      .body(gson.toJson(requestParam))
      .when().post("/wordpress/communities/{id}/notification/{wordpressId}", community.getId(), wordpressId)
      .then().statusCode(200);
    
    // verify
    CommunityNotification result = emService.get()
        .createQuery("FROM CommunityNotification WHERE wordpressId = :wordpressId", CommunityNotification.class)
        .setParameter("wordpressId", wordpressId)
        .getSingleResult();
    assertThat(result.getCommunityId()).isEqualTo(community.getId());
    assertThat(result.getUpdatedNotificationAt().toString()).isEqualTo("2019-01-01T03:10:10Z");

    // call api yyyy-MM-dd HH:mm
    requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2020-01-01 12:10");
    given()
      .body(gson.toJson(requestParam))
      .when().post("/wordpress/communities/{id}/notification/{wordpressId}", community.getId(), wordpressId)
      .then().statusCode(200);

    // verify
    emService.get().refresh(result);
    assertThat(result.getUpdatedNotificationAt().toString()).isEqualTo("2020-01-01T03:10:00Z");

    // call api yyyy-MM-dd HH
    requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2021-01-01 12");
    given()
      .body(gson.toJson(requestParam))
      .when().post("/wordpress/communities/{id}/notification/{wordpressId}", community.getId(), wordpressId)
      .then().statusCode(200);

    // verify
    emService.get().refresh(result);
    assertThat(result.getUpdatedNotificationAt().toString()).isEqualTo("2021-01-01T03:00:00Z");

    // call api yyyy-MM-dd
    requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2022-01-01");
    given()
      .body(gson.toJson(requestParam))
      .when().post("/wordpress/communities/{id}/notification/{wordpressId}", community.getId(), wordpressId)
      .then().statusCode(200);

    // verify
    emService.get().refresh(result);
    assertThat(result.getUpdatedNotificationAt().toString()).isEqualTo("2021-12-31T15:00:00Z");
  }
  
  @Test
  public void communityNotification_invalid_dateformat() {
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2019/01/01 12:10:10");
    String wordpressId = "wordpress1";
    
    // call api
    given()
      .body(gson.toJson(requestParam))
      .when().post("/wordpress/communities/{id}/notification/{wordpressId}", community.getId(), wordpressId)
      .then().statusCode(400);
  }
}
