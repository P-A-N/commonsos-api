package commonsos.integration.message;

import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;

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

public class GetMessageThreadsTest extends IntegrationTest {

  private Community community;
  private User user1;
  private User user2;
  private User user3;
  private Ad ad;
  private MessageThread adThread;
  private MessageThread groupThread;
  private MessageThread directThread;
  private Message groupMessage;
  private String sessionId;
  
  @BeforeEach
  public void setup() {
    community =  create(new Community().setName("community"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad = create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community.getId()).setPoints(TEN));

    adThread = create(new MessageThread()
        .setCommunityId(community.getId())
        .setTitle("adThread")
        .setAdId(ad.getId())
        .setGroup(false)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2)
            )));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(adThread.getId()).setText("adMessage").setCreatedAt(Instant.now().plusSeconds(60)));

    groupThread = create(new MessageThread()
        .setCommunityId(community.getId())
        .setTitle("groupThread")
        .setAdId(null)
        .setGroup(true)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2),
            new MessageThreadParty().setUser(user3)
            )));
    groupMessage = create(new Message().setCreatedBy(user1.getId()).setThreadId(groupThread.getId()).setText("groupMessage").setCreatedAt(Instant.now().plusSeconds(30)));

    directThread = create(new MessageThread()
        .setCommunityId(community.getId())
        .setTitle("directThread")
        .setAdId(null)
        .setGroup(false)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user3)
            )));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(directThread.getId()).setText("directMessage").setCreatedAt(Instant.now().plusSeconds(0)));

    Community otherCommunity =  create(new Community().setName("otherCommunity"));
    MessageThread otherCommunityGroupThread = create(new MessageThread()
        .setCommunityId(otherCommunity.getId())
        .setTitle("groupThread")
        .setAdId(null)
        .setGroup(true)
        .setCreatedBy(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2),
            new MessageThreadParty().setUser(user3)
            )));
    Message otherCommunityGroupMessage = create(new Message().setCreatedBy(user1.getId()).setThreadId(otherCommunityGroupThread.getId()).setText("otherCommunityGroupMessage").setCreatedAt(Instant.now().plusSeconds(30)));
  }
  
  @Test
  public void getMessageThreads() {
    // get threads for user1
    sessionId = login("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}", community.getId())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(adThread.getId().intValue(), groupThread.getId().intValue(), directThread.getId().intValue()))
      .body("messageThreadList.title", contains("adThread", "groupThread", "directThread"))
      .body("messageThreadList.ad.id", contains(ad.getId().intValue()))
      .body("messageThreadList.group", contains(false, true, false))
      .body("messageThreadList.parties.id", contains(
          asList(user2.getId().intValue()), // adThread
          asList(user2.getId().intValue(), user3.getId().intValue()), // groupThread
          asList(user3.getId().intValue()) // directThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // adThread
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // directThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user2.getId().intValue(), // adThread
          user2.getId().intValue(), // groupThread
          user3.getId().intValue() // directThread
          ));

    // get threads for user2
    sessionId = login("user2", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}", community.getId())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(adThread.getId().intValue(), groupThread.getId().intValue()))
      .body("messageThreadList.title", contains("adThread", "groupThread"))
      .body("messageThreadList.ad.id", contains(ad.getId().intValue()))
      .body("messageThreadList.group", contains(false, true))
      .body("messageThreadList.parties.id", contains(
          asList(user2.getId().intValue()), // adThread
          asList(user2.getId().intValue(), user3.getId().intValue()) // groupThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // adThread
          user1.getId().intValue() // groupThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user1.getId().intValue(), // adThread
          user1.getId().intValue() // groupThread
          ));

    // get threads for user3
    sessionId = login("user3", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}", community.getId())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(groupThread.getId().intValue(), directThread.getId().intValue()))
      .body("messageThreadList.title", contains("groupThread", "directThread"))
      .body("messageThreadList.ad.id", emptyIterable())
      .body("messageThreadList.group", contains(true, false))
      .body("messageThreadList.parties.id", contains(
          asList(user2.getId().intValue(), user3.getId().intValue()), // groupThread
          asList(user3.getId().intValue()) // directThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // directThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // directThread
          ));
  }

  @Test
  public void getMessageThreads_memberFilter() {
    // get threads for user1
    sessionId = login("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&memberFilter={memberFilter}", community.getId(), user2.getUsername())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(adThread.getId().intValue(), groupThread.getId().intValue()))
      .body("messageThreadList.title", contains("adThread", "groupThread"))
      .body("messageThreadList.ad.id", contains(ad.getId().intValue()))
      .body("messageThreadList.group", contains(false, true))
      .body("messageThreadList.parties.id", contains(
          asList(user2.getId().intValue()), // adThread
          asList(user2.getId().intValue(), user3.getId().intValue()) // groupThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // adThread
          user1.getId().intValue() // groupThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user2.getId().intValue(), // adThread
          user2.getId().intValue() // groupThread
          ));
  }

  @Test
  public void getMessageThreads_messageFilter() {
    // get threads for user1
    sessionId = login("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&messageFilter={messageFilter}", community.getId(), groupMessage.getText())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(groupThread.getId().intValue()))
      .body("messageThreadList.title", contains("groupThread"))
      .body("messageThreadList.ad.id", emptyIterable())
      .body("messageThreadList.group", contains(true))
      .body("messageThreadList.parties.id", contains(
          asList(user2.getId().intValue(), user3.getId().intValue()) // groupThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue() // groupThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user2.getId().intValue() // groupThread
          ));
  }

  @Test
  public void getMessageThreads_pagination() {
    // prepare
    Long id1 = create(new MessageThread().setCommunityId(community.getId()).setTitle("adThread").setAdId(ad.getId()).setGroup(false).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id2 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id3 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id4 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id5 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id6 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id7 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id8 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id9 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id10 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id11 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id12 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedBy(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id1).setText("page_message").setCreatedAt(Instant.now().minusSeconds(1000)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id2).setText("page_message").setCreatedAt(Instant.now().minusSeconds(1100)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id3).setText("page_message").setCreatedAt(Instant.now().minusSeconds(1200)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id4).setText("page_message").setCreatedAt(Instant.now().minusSeconds(1300)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id5).setText("page_message").setCreatedAt(Instant.now().minusSeconds(1400)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id6).setText("page_message").setCreatedAt(Instant.now().minusSeconds(1500)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id7).setText("page_message").setCreatedAt(Instant.now().minusSeconds(900)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id8).setText("page_message").setCreatedAt(Instant.now().minusSeconds(800)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id9).setText("page_message").setCreatedAt(Instant.now().minusSeconds(700)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id10).setText("page_message").setCreatedAt(Instant.now().minusSeconds(600)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id11).setText("page_message").setCreatedAt(Instant.now().minusSeconds(500)));
    create(new Message().setCreatedBy(user1.getId()).setThreadId(id12).setText("page_message").setCreatedAt(Instant.now().minusSeconds(400)));

    
    // get threads for user1
    sessionId = login("user1", "pass");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "page", "0", "10", "ASC")
      .then().statusCode(200)
      .body("messageThreadList.id", contains(
          id12.intValue(), id11.intValue(), id10.intValue(), id9.intValue(), id8.intValue(),
          id7.intValue(), id1.intValue(), id2.intValue(), id3.intValue(), id4.intValue()))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "page", "1", "10", "ASC")
      .then().statusCode(200)
      .body("messageThreadList.id", contains(
          id5.intValue(), id6.intValue()))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 2 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "page", "2", "10", "ASC")
      .then().statusCode(200)
      .body("messageThreadList.id", emptyIterable())
      .body("pagination.page", equalTo(2))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "page", "0", "10", "DESC")
      .then().statusCode(200)
      .body("messageThreadList.id", contains(
          id6.intValue(), id5.intValue(), id4.intValue(), id3.intValue(), id2.intValue(),
          id1.intValue(), id7.intValue(), id8.intValue(), id9.intValue(), id10.intValue()))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "page", "1", "10", "DESC")
      .then().statusCode(200)
      .body("messageThreadList.id", contains(
          id11.intValue(), id12.intValue()))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 2 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "page", "2", "10", "DESC")
      .then().statusCode(200)
      .body("messageThreadList.id", emptyIterable())
      .body("pagination.page", equalTo(2))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
