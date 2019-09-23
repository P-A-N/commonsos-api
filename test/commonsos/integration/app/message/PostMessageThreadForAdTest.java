package commonsos.integration.app.message;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class PostMessageThreadForAdTest extends IntegrationTest {

  private Community community;
  private User adCreator;
  private User user1;
  private User user2;
  private Ad ad;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setStatus(PUBLIC).setName("community"));
    adCreator =  create(new User().setUsername("adCreator").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad =  create(new Ad().setCreatedUserId(adCreator.getId()).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));

    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
  }
  
  @Test
  public void messageThreadForAd() {
    // call api from user1 (1)
    sessionId = loginApp("user1", "pass");
    int id1 = given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/app/v{v}/message-threads/for-ad/{adId}", APP_API_VERSION.getMajor(), ad.getId())
      .then().statusCode(200)
      .body("id", notNullValue())
      .body("ad.id", equalTo(ad.getId().intValue()))
      .body("ad.createdBy.id", equalTo(adCreator.getId().intValue()))
      .body("communityId", equalTo(ad.getCommunityId().intValue()))
      .body("title", equalTo("title"))
      .body("personalTitle", nullValue())
      .body("parties.id", contains(adCreator.getId().intValue()))
      .body("creator.id", equalTo(user1.getId().intValue()))
      .body("counterParty.id", equalTo(adCreator.getId().intValue()))
      .body("lastMessage", nullValue())
      .body("unread", equalTo(false))
      .body("group", equalTo(false))
      .body("photoUrl", nullValue())
      .body("createdAt", notNullValue())
      .extract().path("id");
    
    // call api from user2 (1)
    sessionId = loginApp("user2", "pass");
    int id2 = given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/app/v{v}/message-threads/for-ad/{adId}", APP_API_VERSION.getMajor(), ad.getId())
      .then().statusCode(200)
      .body("id", notNullValue())
      .body("ad.id", equalTo(ad.getId().intValue()))
      .body("ad.createdBy.id", equalTo(adCreator.getId().intValue()))
      .body("communityId", equalTo(ad.getCommunityId().intValue()))
      .body("title", equalTo("title"))
      .body("personalTitle", nullValue())
      .body("parties.id", contains(adCreator.getId().intValue()))
      .body("creator.id", equalTo(user2.getId().intValue()))
      .body("counterParty.id", equalTo(adCreator.getId().intValue()))
      .body("lastMessage", nullValue())
      .body("unread", equalTo(false))
      .body("group", equalTo(false))
      .body("photoUrl", nullValue())
      .body("createdAt", notNullValue())
      .extract().path("id");

    assertThat(id1).isNotEqualTo(id2);

    // call api from user1 (2)
    sessionId = loginApp("user1", "pass");
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/app/v{v}/message-threads/for-ad/{adId}", APP_API_VERSION.getMajor(), ad.getId())
      .then().statusCode(200)
      .body("id", equalTo(id1))
      .body("parties.id", contains(adCreator.getId().intValue()));
    
    // call api from user2 (2)
    sessionId = loginApp("user2", "pass");
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/app/v{v}/message-threads/for-ad/{adId}", APP_API_VERSION.getMajor(), ad.getId())
      .then().statusCode(200)
      .body("id", equalTo(id2))
      .body("parties.id", contains(adCreator.getId().intValue()));
  }
}
