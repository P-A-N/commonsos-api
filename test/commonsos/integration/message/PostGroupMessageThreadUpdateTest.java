package commonsos.integration.message;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.community.Community;
import commonsos.repository.message.MessageThread;
import commonsos.repository.user.User;

public class PostGroupMessageThreadUpdateTest extends IntegrationTest {

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
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community)));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community)));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community)));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setJoinedCommunities(asList(otherCommunity)));

    sessionId = login("user1", "pass");

    // create group chat
    Map<String, Object> requestParam = new HashMap<>();
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
  public void groupMessageThreadUpdate() {
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("title", "title2");
    requestParam.put("memberIds", asList(user3.getId()));
    
    // call api
    int id = given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/message-threads/{id}/group", messageThreadId)
      .then().statusCode(200)
      .body("id", equalTo(messageThreadId.intValue()))
      .body("ad.id", nullValue())
      .body("title", equalTo("title2"))
      .body("parties.id", contains(user2.getId().intValue(), user3.getId().intValue()))
      .body("creator.id", equalTo(user1.getId().intValue()))
      .body("counterParty.id", equalTo(user2.getId().intValue()))
      .body("lastMessage", nullValue())
      .body("unread", equalTo(false))
      .body("group", equalTo(true))
      .body("createdAt", notNullValue())
      .extract().path("id");
    
    // verify db
    MessageThread messageThread = emService.get().find(MessageThread.class, (long) id);
    assertThat(messageThread.getTitle()).isEqualTo("title2");
    assertThat(messageThread.getAdId()).isNull();
    assertThat(messageThread.getCreatedBy()).isEqualTo(user1.getId());
    assertThat(messageThread.isGroup()).isEqualTo(true);
    
    messageThread.getParties().sort((a,b) -> a.getUser().getId().compareTo(b.getUser().getId()));
    assertThat(messageThread.getParties().size()).isEqualTo(3);
    assertThat(messageThread.getParties().get(0).getUser().getId()).isEqualTo(user1.getId());
    assertThat(messageThread.getParties().get(1).getUser().getId()).isEqualTo(user2.getId());
    assertThat(messageThread.getParties().get(2).getUser().getId()).isEqualTo(user3.getId());
  }

  @Test
  public void groupMessageThreadUpdate_otherCommunityUser() {
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("title", "title2");
    requestParam.put("memberIds", asList(otherCommunityUser.getId()));
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/message-threads/{id}/group", messageThreadId)
      .then().statusCode(200);
  }
}
