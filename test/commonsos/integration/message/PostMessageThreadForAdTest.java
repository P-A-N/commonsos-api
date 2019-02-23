package commonsos.integration.message;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.User;

public class PostMessageThreadForAdTest extends IntegrationTest {

  private Community community;
  private User adCreator;
  private User user;
  private Ad ad;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    adCreator =  create(new User().setUsername("adCreator").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad =  create(new Ad().setCreatedBy(adCreator.getId()).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));

    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));

    sessionId = login("user", "pass");
  }
  
  @Test
  public void messageThreadForAd() {
    // call api
    int id = given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/for-ad/{adId}", ad.getId())
      .then().statusCode(200)
      .body("id", notNullValue())
      .body("ad.id", equalTo(ad.getId().intValue()))
      .body("ad.createdBy.id", equalTo(adCreator.getId().intValue()))
      .body("communityId", equalTo(ad.getCommunityId().intValue()))
      .body("title", equalTo("title"))
      .body("personalTitle", nullValue())
      .body("parties.id", contains(adCreator.getId().intValue()))
      .body("creator.id", equalTo(user.getId().intValue()))
      .body("counterParty.id", equalTo(adCreator.getId().intValue()))
      .body("lastMessage", nullValue())
      .body("unread", equalTo(false))
      .body("group", equalTo(false))
      .body("photoUrl", nullValue())
      .body("createdAt", notNullValue())
      .extract().path("id");
    
    // verify db
    MessageThread messageThread = emService.get().find(MessageThread.class, (long) id);
    assertThat(messageThread.getTitle()).isEqualTo("title");
    assertThat(messageThread.getAdId()).isEqualTo(ad.getId());
    assertThat(messageThread.getCreatedBy()).isEqualTo(user.getId());
    assertThat(messageThread.isGroup()).isEqualTo(false);
    
    messageThread.getParties().sort((a,b) -> a.getUser().getId().compareTo(b.getUser().getId()));
    assertThat(messageThread.getParties().size()).isEqualTo(2);
    assertThat(messageThread.getParties().get(0).getUser().getId()).isEqualTo(adCreator.getId());
    assertThat(messageThread.getParties().get(1).getUser().getId()).isEqualTo(user.getId());
  }
}
