package commonsos.integration.app.ad;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetAdTest extends IntegrationTest {

  private Community community;
  private User creator;
  private User otherUser;
  private Ad publicAd;
  private Ad privateAd;
  private String sessionId;
  
  @BeforeEach
  public void setupData() throws Exception {
    community = create(new Community().setName("community").setPublishStatus(PUBLIC));
    creator = create(new User().setUsername("creator").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherUser = create(new User().setUsername("otherUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    publicAd = create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(creator.getId()).setCommunityId(community.getId()).setPoints(TEN));
    privateAd = create(new Ad().setPublishStatus(PRIVATE).setCreatedUserId(creator.getId()).setCommunityId(community.getId()).setPoints(TEN));
  }
  
  @Test
  public void getAd() {
    // get public ad by creator
    sessionId = loginApp("creator", "pass");
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/ads/{id}", APP_API_VERSION.getMajor(), publicAd.getId())
      .then().statusCode(200)
      .body("id", equalTo(publicAd.getId().intValue()));

    // get private ad by creator
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/ads/{id}", APP_API_VERSION.getMajor(), privateAd.getId())
      .then().statusCode(200)
      .body("id", equalTo(privateAd.getId().intValue()));

    // get public ad by other user
    sessionId = loginApp("otherUser", "pass");
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/ads/{id}", APP_API_VERSION.getMajor(), publicAd.getId())
      .then().statusCode(200)
      .body("id", equalTo(publicAd.getId().intValue()));

    // get private ad by other user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/ads/{id}", APP_API_VERSION.getMajor(), privateAd.getId())
      .then().statusCode(400);
  }
}
