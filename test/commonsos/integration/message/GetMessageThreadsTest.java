package commonsos.integration.message;

import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;

public class GetMessageThreadsTest extends IntegrationTest {

  private Community community;
  private User user1;
  private User user2;
  private User user3;
  private Ad ad;
  private MessageThread adThread;
  private MessageThread groupThread;
  private MessageThread directThread;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
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
    create(new Message().setThreadId(adThread.getId()).setText("adMessage").setCreatedAt(Instant.now().plusSeconds(60)));

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
    create(new Message().setThreadId(groupThread.getId()).setText("groupMessage").setCreatedAt(Instant.now().plusSeconds(30)));

    directThread = create(new MessageThread()
        .setTitle("directThread")
        .setAdId(null)
        .setGroup(false)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user3)
            )));
    create(new Message().setThreadId(directThread.getId()).setText("directMessage").setCreatedAt(Instant.now().plusSeconds(0)));
  }
  
  @Test
  public void getMessageThreads() {
    // get threads for user1
    sessionId = login("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads")
      .then().statusCode(200)
      .body("id", contains(adThread.getId().intValue(), groupThread.getId().intValue(), directThread.getId().intValue()))
      .body("title", contains("adThread", "groupThread", "directThread"))
      .body("ad.id", contains(ad.getId().intValue()))
      .body("group", contains(false, true, false))
      .body("parties.id", contains(
          asList(user2.getId().intValue()), // adThread
          asList(user2.getId().intValue(), user3.getId().intValue()), // groupThread
          asList(user3.getId().intValue()) // directThread
          ))
      .body("creator.id", contains(
          user1.getId().intValue(), // adThread
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // directThread
          ))
      .body("counterParty.id", contains(
          user2.getId().intValue(), // adThread
          user2.getId().intValue(), // groupThread
          user3.getId().intValue() // directThread
          ));

    // get threads for user2
    sessionId = login("user2", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads")
      .then().statusCode(200)
      .body("id", contains(adThread.getId().intValue(), groupThread.getId().intValue()))
      .body("title", contains("adThread", "groupThread"))
      .body("ad.id", contains(ad.getId().intValue()))
      .body("group", contains(false, true))
      .body("parties.id", contains(
          asList(user2.getId().intValue()), // adThread
          asList(user2.getId().intValue(), user3.getId().intValue()) // groupThread
          ))
      .body("creator.id", contains(
          user1.getId().intValue(), // adThread
          user1.getId().intValue() // groupThread
          ))
      .body("counterParty.id", contains(
          user1.getId().intValue(), // adThread
          user1.getId().intValue() // groupThread
          ));

    // get threads for user3
    sessionId = login("user3", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads")
      .then().statusCode(200)
      .body("id", contains(groupThread.getId().intValue(), directThread.getId().intValue()))
      .body("title", contains("groupThread", "directThread"))
      .body("ad.id", emptyIterable())
      .body("group", contains(true, false))
      .body("parties.id", contains(
          asList(user2.getId().intValue(), user3.getId().intValue()), // groupThread
          asList(user3.getId().intValue()) // directThread
          ))
      .body("creator.id", contains(
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // directThread
          ))
      .body("counterParty.id", contains(
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // directThread
          ));
  }
}
