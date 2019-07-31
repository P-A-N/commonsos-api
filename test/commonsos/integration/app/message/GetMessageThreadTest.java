package commonsos.integration.app.message;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;

public class GetMessageThreadTest extends IntegrationTest {

  private Community community;
  private User user1;
  private User user2;
  private User user3;
  private Ad ad;
  private MessageThread adThread;
  private MessageThread groupThread;
  private MessageThread directThread;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setStatus(PUBLIC).setName("community"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad = create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community.getId()).setPoints(TEN));

    adThread = create(new MessageThread()
        .setTitle("adThread")
        .setAdId(ad.getId())
        .setGroup(false)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2)
            )));
    create(new Message().setThreadId(adThread.getId()).setText("adMessage"));
    Thread.sleep(1);

    groupThread = create(new MessageThread()
        .setTitle("groupThread")
        .setAdId(null)
        .setGroup(true)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2),
            new MessageThreadParty().setUser(user3)
            )));
    create(new Message().setThreadId(groupThread.getId()).setText("groupMessage"));
    Thread.sleep(1);

    directThread = create(new MessageThread()
        .setTitle("directThread")
        .setAdId(null)
        .setGroup(false)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user3)
            )));
    create(new Message().setThreadId(directThread.getId()).setText("directMessage"));

    sessionId = login("user1", "pass");
  }
  
  @Test
  public void getMessageThreadsById() {
    // get adThreads
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads/{id}", adThread.getId())
      .then().statusCode(200)
      .body("id", equalTo(adThread.getId().intValue()))
      .body("title", equalTo("adThread"))
      .body("ad.id", equalTo(ad.getId().intValue()))
      .body("group", equalTo(false))
      .body("parties.id", contains(
          user2.getId().intValue()
          ))
      .body("creator.id", equalTo(
          user1.getId().intValue()
          ))
      .body("counterParty.id", equalTo(
          user2.getId().intValue()
          ));

    // get groupThread
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads/{id}", groupThread.getId())
      .then().statusCode(200)
      .body("id", equalTo(groupThread.getId().intValue()))
      .body("title", equalTo("groupThread"))
      .body("ad.id", nullValue())
      .body("group", equalTo(true))
      .body("parties.id", contains(
          user2.getId().intValue(),
          user3.getId().intValue()
          ))
      .body("creator.id", equalTo(
          user1.getId().intValue()
          ))
      .body("counterParty.id", equalTo(
          user2.getId().intValue()
          ));

    // get directThread
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads/{id}", directThread.getId())
      .then().statusCode(200)
      .body("id", equalTo(directThread.getId().intValue()))
      .body("title", equalTo("directThread"))
      .body("ad.id", nullValue())
      .body("group", equalTo(false))
      .body("parties.id", contains(
          user3.getId().intValue()
          ))
      .body("creator.id", equalTo(
          user1.getId().intValue()
          ))
      .body("counterParty.id", equalTo(
          user3.getId().intValue()
          ));
  }

  @Test
  public void getMessageThreadsById_notMember() {
    // login with user who is not a member of adThreads
    sessionId = login("user3", "pass");
    
    // get adThreads
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads/{id}", adThread.getId())
      .then().statusCode(403);
  }
}
