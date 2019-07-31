package commonsos.integration.app.message;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import commonsos.util.MessageUtil;

public class PostMessageThreadUnsubscribeTest extends IntegrationTest {

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
  public void setup() {
    community =  create(new Community().setStatus(PUBLIC).setName("community"));
    otherCommunity =  create(new Community().setStatus(PUBLIC).setName("otherCommunity"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));
    ad =  create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));

    sessionId = login("user1", "pass");

    // create group thread
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
    groupThreadId = (long) id;

    // create ad thread
     id = given()
        .cookie("JSESSIONID", sessionId)
        .when().post("/message-threads/for-ad/{adId}", ad.getId())
        .then().statusCode(200)
        .extract().path("id");
     adThreadId = (long) id;
  }
  
  @Test
  public void messageThreadUnsubscribe() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/{id}/unsubscribe", groupThreadId)
      .then().statusCode(200);

    MessageThread mt = emService.get().createQuery("FROM MessageThread WHERE id = :id", MessageThread.class)
        .setParameter("id", groupThreadId)
        .getSingleResult();
    assertThat(mt.getParties()).extracting(MessageThreadParty::getUser).extracting(User::getId).doesNotContain(user1.getId());
    
    List<Message> mList = emService.get().createQuery("FROM Message WHERE thread_id = :id ORDER BY id DESC", Message.class)
        .setParameter("id", groupThreadId)
        .getResultList();
    assertThat(mList.get(0).getCreatedBy()).isEqualTo(MessageUtil.getSystemMessageCreatorId());
  }
  
  @Test
  public void updateMessageThreadPersonalTitle_not_member() {
    // login with non member of thread
    sessionId = login("user3", "pass");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/{id}/unsubscribe", groupThreadId)
      .then().statusCode(400);
  }
  
  @Test
  public void updateMessageThreadPersonalTitle_notGroup() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/{id}/unsubscribe", adThreadId)
      .then().statusCode(400);
  }
}
