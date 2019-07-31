package commonsos.integration.app.message;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;

public class GetUnreadMessageThreadCountTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User user1;
  private User user2;
  private User user3;
  private String sessionId;
  
  @BeforeEach
  public void setup() {
    community1 =  create(new Community().setStatus(PUBLIC).setName("community1"));
    community2 =  create(new Community().setStatus(PUBLIC).setName("community2"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));

    // unread threads (community1)
    for (int i = 0; i < 2; i++) {
      Long threadId = create(new MessageThread().setCommunityId(community1.getId()).setTitle("thread" + i).setAdId(null)
          .setGroup(true).setCreatedBy(user1.getId()).setParties(asList(
              new MessageThreadParty().setUser(user1).setVisitedAt(Instant.now().minusSeconds(100)),
              new MessageThreadParty().setUser(user2).setVisitedAt(Instant.now().minusSeconds(100))))).getId();
      create(new Message().setCreatedBy(user1.getId()).setThreadId(threadId).setText("message" + i));
    }

    // read threads (community1)
    for (int i = 0; i < 3; i++) {
      Long threadId = create(new MessageThread().setCommunityId(community1.getId()).setTitle("thread" + i).setAdId(null)
          .setGroup(true).setCreatedBy(user1.getId()).setParties(asList(
              new MessageThreadParty().setUser(user1).setVisitedAt(Instant.now().plusSeconds(100)),
              new MessageThreadParty().setUser(user2).setVisitedAt(Instant.now().plusSeconds(100))))).getId();
      create(new Message().setCreatedBy(user1.getId()).setThreadId(threadId).setText("message" + i));
    }

    // unread threads (community2)
    for (int i = 0; i < 4; i++) {
      Long threadId = create(new MessageThread().setCommunityId(community2.getId()).setTitle("thread" + i).setAdId(null)
          .setGroup(true).setCreatedBy(user1.getId()).setParties(asList(
              new MessageThreadParty().setUser(user1).setVisitedAt(Instant.now().minusSeconds(100)),
              new MessageThreadParty().setUser(user2).setVisitedAt(Instant.now().minusSeconds(100))))).getId();
      create(new Message().setCreatedBy(user1.getId()).setThreadId(threadId).setText("message" + i));
    }

    // read threads (community2)
    for (int i = 0; i < 5; i++) {
      Long threadId = create(new MessageThread().setCommunityId(community2.getId()).setTitle("thread" + i).setAdId(null)
          .setGroup(true).setCreatedBy(user1.getId()).setParties(asList(
              new MessageThreadParty().setUser(user1).setVisitedAt(Instant.now().plusSeconds(100)),
              new MessageThreadParty().setUser(user2).setVisitedAt(Instant.now().plusSeconds(100))))).getId();
      create(new Message().setCreatedBy(user1.getId()).setThreadId(threadId).setText("message" + i));
    }

  }
  
  @Test
  public void getMessageThreads() {
    // get threads for user1
    sessionId = login("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads/unread-count?communityId={communityId}", community1.getId())
      .then().statusCode(200)
      .body("count", equalTo(2));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads/unread-count?communityId={communityId}", community2.getId())
      .then().statusCode(200)
      .body("count", equalTo(4));
  }
}
