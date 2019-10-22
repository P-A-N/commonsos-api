package commonsos.integration.app.message;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

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

public class SearchMessageThreadsTest extends IntegrationTest {

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
  public void setup() throws Exception {
    community =  create(new Community().setPublishStatus(PUBLIC).setName("community"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad = create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(user1.getId()).setCommunityId(community.getId()).setPoints(TEN));

    adThread = create(new MessageThread()
        .setCommunityId(community.getId())
        .setTitle("adThread")
        .setAdId(ad.getId())
        .setGroup(false)
        .setCreatedUserId(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2)
            )));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(adThread.getId()).setText("adMessage"));
    Thread.sleep(1);

    groupThread = create(new MessageThread()
        .setCommunityId(community.getId())
        .setTitle("groupThread")
        .setAdId(null)
        .setGroup(true)
        .setCreatedUserId(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2),
            new MessageThreadParty().setUser(user3)
            )));
    groupMessage = create(new Message().setCreatedUserId(user1.getId()).setThreadId(groupThread.getId()).setText("groupMessage"));
    Thread.sleep(1);

    directThread = create(new MessageThread()
        .setCommunityId(community.getId())
        .setTitle("directThread")
        .setAdId(null)
        .setGroup(false)
        .setCreatedUserId(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user3)
            )));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(directThread.getId()).setText("directMessage"));
    Thread.sleep(1);

    Community otherCommunity =  create(new Community().setPublishStatus(PUBLIC).setName("otherCommunity"));
    MessageThread otherCommunityGroupThread = create(new MessageThread()
        .setCommunityId(otherCommunity.getId())
        .setTitle("groupThread")
        .setAdId(null)
        .setGroup(true)
        .setCreatedUserId(user1.getId())
        .setParties(asList(
            new MessageThreadParty().setUser(user1),
            new MessageThreadParty().setUser(user2),
            new MessageThreadParty().setUser(user3)
            )));
    Message otherCommunityGroupMessage = create(new Message().setCreatedUserId(user1.getId()).setThreadId(otherCommunityGroupThread.getId()).setText("otherCommunityGroupMessage"));
  }
  
  @Test
  public void getMessageThreads() {
    // get threads for user1
    sessionId = loginApp("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}", APP_API_VERSION.getMajor(), community.getId())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(directThread.getId().intValue(), groupThread.getId().intValue(), adThread.getId().intValue()))
      .body("messageThreadList.title", contains("directThread", "groupThread", "adThread"))
      .body("messageThreadList.ad.id", contains(ad.getId().intValue()))
      .body("messageThreadList.group", contains(false, true, false))
      .body("messageThreadList.parties.id", contains(
          asList(user3.getId().intValue()), // directThread
          asList(user2.getId().intValue(), user3.getId().intValue()), // groupThread
          asList(user2.getId().intValue()) // adThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // directThread
          user1.getId().intValue(), // groupThread
          user1.getId().intValue()  // adThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user3.getId().intValue(), // directThread
          user2.getId().intValue(), // groupThread
          user2.getId().intValue()  // adThread
          ));

    // get threads for user2
    sessionId = loginApp("user2", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}", APP_API_VERSION.getMajor(), community.getId())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(groupThread.getId().intValue(), adThread.getId().intValue()))
      .body("messageThreadList.title", contains("groupThread", "adThread"))
      .body("messageThreadList.ad.id", contains(ad.getId().intValue()))
      .body("messageThreadList.group", contains(true, false))
      .body("messageThreadList.parties.id", contains(
          asList(user2.getId().intValue(), user3.getId().intValue()), // groupThread
          asList(user2.getId().intValue()) // adThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // adThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // adThread
          ));

    // get threads for user3
    sessionId = loginApp("user3", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}", APP_API_VERSION.getMajor(), community.getId())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(directThread.getId().intValue(), groupThread.getId().intValue()))
      .body("messageThreadList.title", contains("directThread", "groupThread"))
      .body("messageThreadList.ad.id", emptyIterable())
      .body("messageThreadList.group", contains(false, true))
      .body("messageThreadList.parties.id", contains(
          asList(user3.getId().intValue()), // directThread
          asList(user2.getId().intValue(), user3.getId().intValue()) // groupThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // directThread
          user1.getId().intValue() // groupThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user1.getId().intValue(), // directThread
          user1.getId().intValue() // groupThread
          ));
  }

  @Test
  public void getMessageThreads_memberFilter() {
    // get threads for user1
    sessionId = loginApp("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}&memberFilter={memberFilter}", APP_API_VERSION.getMajor(), community.getId(), user2.getUsername())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(groupThread.getId().intValue(), adThread.getId().intValue()))
      .body("messageThreadList.title", contains("groupThread", "adThread"))
      .body("messageThreadList.ad.id", contains(ad.getId().intValue()))
      .body("messageThreadList.group", contains(true, false))
      .body("messageThreadList.parties.id", contains(
          asList(user2.getId().intValue(), user3.getId().intValue()), // groupThread
          asList(user2.getId().intValue()) // adThread
          ))
      .body("messageThreadList.creator.id", contains(
          user1.getId().intValue(), // groupThread
          user1.getId().intValue() // adThread
          ))
      .body("messageThreadList.counterParty.id", contains(
          user2.getId().intValue(), // groupThread
          user2.getId().intValue() // adThread
          ));
  }

  @Test
  public void getMessageThreads_messageFilter() {
    // get threads for user1
    sessionId = loginApp("user1", "pass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}&messageFilter={messageFilter}", APP_API_VERSION.getMajor(), community.getId(), groupMessage.getText())
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
  public void getMessageThreads_pagination() throws Exception {
    // prepare
    Long id1 = create(new MessageThread().setCommunityId(community.getId()).setTitle("adThread").setAdId(ad.getId()).setGroup(false).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id2 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id3 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id4 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id5 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id6 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id7 = create(new MessageThread().setCommunityId(community.getId()).setTitle("groupThread").setAdId(null).setGroup(true).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id8 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id9 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id10 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id11 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    Long id12 = create(new MessageThread().setCommunityId(community.getId()).setTitle("directThread").setAdId(null).setGroup(false).setCreatedUserId(user1.getId()).setParties(asList(new MessageThreadParty().setUser(user1),new MessageThreadParty().setUser(user2)))).getId();
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id1).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id2).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id3).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id4).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id5).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id6).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id7).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id8).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id9).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id10).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id11).setText("page_message"));
    create(new Message().setCreatedUserId(user1.getId()).setThreadId(id12).setText("page_message"));

    
    // get threads for user1
    sessionId = loginApp("user1", "pass");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "page", "0", "10", "ASC")
      .then().statusCode(200)
      .body("messageThreadList.id", contains(
          id12.intValue(), id11.intValue(), id10.intValue(), id9.intValue(), id8.intValue(),
          id7.intValue(), id6.intValue(), id5.intValue(), id4.intValue(), id3.intValue()))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "page", "1", "10", "ASC")
      .then().statusCode(200)
      .body("messageThreadList.id", contains(
          id2.intValue(), id1.intValue()))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 2 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "page", "2", "10", "ASC")
      .then().statusCode(200)
      .body("messageThreadList.id", emptyIterable())
      .body("pagination.page", equalTo(2))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "page", "0", "10", "DESC")
      .then().statusCode(200)
      .body("messageThreadList.id", contains(
          id1.intValue(), id2.intValue(), id3.intValue(), id4.intValue(), id5.intValue(),
          id6.intValue(), id7.intValue(), id8.intValue(), id9.intValue(), id10.intValue()))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "page", "1", "10", "DESC")
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
      .when().get("/app/v{v}/message-threads?communityId={communityId}&messageFilter={messageFilter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "page", "2", "10", "DESC")
      .then().statusCode(200)
      .body("messageThreadList.id", emptyIterable())
      .body("pagination.page", equalTo(2))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
