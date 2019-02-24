package commonsos.integration.community;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityNotification;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class PostCommunityNotificationTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User admin1;
  private User admin2;
  private User user;
  private String sessionId;
  
  @Before
  public void setup() {
    community1 =  create(new Community().setName("community1"));
    community2 =  create(new Community().setName("community2"));
    admin1 =  create(new User().setUsername("admin1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    admin2 =  create(new User().setUsername("admin2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community2))));
    update(community1.setAdminUser(admin1));
    update(community2.setAdminUser(admin2));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
  }
  
  @Test
  public void communityNotification_valid_dateformat() {
    // login
    sessionId = login("admin1", "pass");
    
    // prepare yyyy-MM-dd HH:mm:ss
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2019-01-01 12:10:10");
    String wordpressId = "wordpress1";
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{id}/notification/{wordpressId}", community1.getId(), wordpressId)
      .then().statusCode(200);
    
    // verify
    CommunityNotification result = emService.get()
        .createQuery("FROM CommunityNotification WHERE wordpressId = :wordpressId", CommunityNotification.class)
        .setParameter("wordpressId", wordpressId)
        .getSingleResult();
    assertThat(result.getCommunityId()).isEqualTo(community1.getId());
    assertThat(result.getUpdatedAt().toString()).isEqualTo("2019-01-01T03:10:10Z");

    // call api yyyy-MM-dd HH:mm
    requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2020-01-01 12:10");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{id}/notification/{wordpressId}", community1.getId(), wordpressId)
      .then().statusCode(200);

    // verify
    emService.get().refresh(result);
    assertThat(result.getUpdatedAt().toString()).isEqualTo("2020-01-01T03:10:00Z");

    // call api yyyy-MM-dd HH
    requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2021-01-01 12");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{id}/notification/{wordpressId}", community1.getId(), wordpressId)
      .then().statusCode(200);

    // verify
    emService.get().refresh(result);
    assertThat(result.getUpdatedAt().toString()).isEqualTo("2021-01-01T03:00:00Z");

    // call api yyyy-MM-dd
    requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2022-01-01");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{id}/notification/{wordpressId}", community1.getId(), wordpressId)
      .then().statusCode(200);

    // verify
    emService.get().refresh(result);
    assertThat(result.getUpdatedAt().toString()).isEqualTo("2021-12-31T15:00:00Z");
  }
  
  @Test
  public void communityNotification_notAdmin() {
    // login
    sessionId = login("admin2", "pass");
    
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2019-01-01 12:10:10");
    String wordpressId = "wordpress1";
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{id}/notification/{wordpressId}", community1.getId(), wordpressId)
      .then().statusCode(400);

    // login
    sessionId = login("user", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{id}/notification/{wordpressId}", community1.getId(), wordpressId)
      .then().statusCode(400);
  }
  
  @Test
  public void communityNotification_invalid_dateformat() {
    // login
    sessionId = login("admin1", "pass");
    
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("updatedAt", "2019/01/01 12:10:10");
    String wordpressId = "wordpress1";
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/communities/{id}/notification/{wordpressId}", community1.getId(), wordpressId)
      .then().statusCode(400);
  }
}
