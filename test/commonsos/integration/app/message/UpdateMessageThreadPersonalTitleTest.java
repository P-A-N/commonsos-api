package commonsos.integration.app.message;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class UpdateMessageThreadPersonalTitleTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user1;
  private User user2;
  private User user3;
  private User otherCommunityUser;
  private Ad ad;
  private Long groupThreadId;
  private Long adThreadId;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setPublishStatus(PUBLIC).setName("community"));
    otherCommunity =  create(new Community().setPublishStatus(PUBLIC).setName("otherCommunity"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));
    ad =  create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(user1.getId()).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));

    sessionId = loginApp("user1", "pass");

    // create group thread
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("title", "title");
    requestParam.put("memberIds", asList(user2.getId()));
    int id = given()
        .cookie("JSESSIONID", sessionId)
        .body(gson.toJson(requestParam))
        .when().post("/app/v{v}/message-threads/group", APP_API_VERSION.getMajor())
        .then().statusCode(200)
        .extract().path("id");
    groupThreadId = (long) id;

    // create ad thread
     id = given()
        .cookie("JSESSIONID", sessionId)
        .when().post("/app/v{v}/message-threads/for-ad/{adId}", APP_API_VERSION.getMajor(), ad.getId())
        .then().statusCode(200)
        .extract().path("id");
     adThreadId = (long) id;
  }
  
  @Test
  public void updateMessageThreadPersonalTitle() {
    // update personal title
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("personalTitle", "pTitle");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/message-threads/{id}/title", APP_API_VERSION.getMajor(), groupThreadId)
      .then().statusCode(200)
      .body("id", equalTo(groupThreadId.intValue()))
      .body("title", equalTo("title"))
      .body("personalTitle", equalTo("pTitle"));
  }
  
  @Test
  public void updateMessageThreadPersonalTitle_not_member() {
    // login with non member of thread
    sessionId = loginApp("user3", "pass");
    
    // update personal title
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("personalTitle", "pTitle");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/message-threads/{id}/title", APP_API_VERSION.getMajor(), groupThreadId)
      .then().statusCode(400);
  }
  
  @Test
  public void updateMessageThreadPersonalTitle_notGroup() {
    // update personal title
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("personalTitle", "pTitle");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/message-threads/{id}/title", APP_API_VERSION.getMajor(), adThreadId)
      .then().statusCode(400);
  }
}
