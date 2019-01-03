package commonsos.integration.message;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.User;

public class PostMessageThreadWithUserTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user1;
  private User user2;
  private User otherCommunityUser;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    otherCommunity =  create(new Community().setName("otherCommunity"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityList(asList(otherCommunity)));

    sessionId = login("user1", "pass");
  }
  
  @Test
  public void messageThreadWithUser() {
    // call api
    int id = given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/user/{userId}", user2.getId())
      .then().statusCode(200)
      .body("id", notNullValue())
      .body("ad.id", nullValue())
      .body("title", nullValue())
      .body("parties.id", contains(user2.getId().intValue()))
      .body("creator.id", equalTo(user1.getId().intValue()))
      .body("counterParty.id", equalTo(user2.getId().intValue()))
      .body("lastMessage", nullValue())
      .body("unread", equalTo(false))
      .body("group", equalTo(false))
      .body("createdAt", notNullValue())
      .extract().path("id");
    
    // verify db
    MessageThread messageThread = emService.get().find(MessageThread.class, (long) id);
    assertThat(messageThread.getTitle()).isNull();
    assertThat(messageThread.getAdId()).isNull();
    assertThat(messageThread.getCreatedBy()).isEqualTo(user1.getId());
    assertThat(messageThread.isGroup()).isEqualTo(false);
    
    messageThread.getParties().sort((a,b) -> a.getUser().getId().compareTo(b.getUser().getId()));
    assertThat(messageThread.getParties().size()).isEqualTo(2);
    assertThat(messageThread.getParties().get(0).getUser().getId()).isEqualTo(user1.getId());
    assertThat(messageThread.getParties().get(1).getUser().getId()).isEqualTo(user2.getId());
  }

  @Test
  public void messageThreadWithUser_otherCommunityUser() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/user/{userId}", otherCommunityUser.getId())
      .then().statusCode(200);
  }
}
