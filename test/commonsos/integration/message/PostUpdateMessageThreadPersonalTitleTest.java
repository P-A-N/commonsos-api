package commonsos.integration.message;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class PostUpdateMessageThreadPersonalTitleTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user1;
  private User user2;
  private User user3;
  private User otherCommunityUser;
  private Long messageThreadId;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    otherCommunity =  create(new Community().setName("otherCommunity"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityList(asList(otherCommunity)));

    sessionId = login("user1", "pass");

    // create group chat
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("title", "title");
    requestParam.put("memberIds", asList(user2.getId()));
    int id = given()
        .cookie("JSESSIONID", sessionId)
        .body(gson.toJson(requestParam))
        .when().post("/message-threads/group")
        .then().statusCode(200)
        .extract().path("id");
    messageThreadId = (long) id;
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
      .when().post("/message-threads/{id}/title", messageThreadId)
      .then().statusCode(200)
      .body("id", equalTo(messageThreadId.intValue()))
      .body("title", equalTo("title"))
      .body("personalTitle", equalTo("pTitle"));
  }
  
  @Test
  public void updateMessageThreadPersonalTitle_not_member() {
    // login with non member of thread
    sessionId = login("user3", "pass");
    
    // update personal title
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("personalTitle", "pTitle");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/message-threads/{id}/title", messageThreadId)
      .then().statusCode(400);
  }
}
