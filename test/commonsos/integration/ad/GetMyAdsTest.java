package commonsos.integration.ad;

import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetMyAdsTest extends IntegrationTest {

  private Community community;
  private User user1;
  private User user2;
  private Ad ad1_1;
  private Ad ad1_2;
  private Ad ad2;
  private String sessionId;
  
  @BeforeEach
  public void setupData() {
    community = create(new Community().setName("community"));
    user1 = create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 = create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad1_1 = create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community.getId()).setPoints(TEN));
    ad1_2 = create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community.getId()).setPoints(TEN));
    ad2 = create(new Ad().setCreatedBy(user2.getId()).setCommunityId(community.getId()).setPoints(TEN));
  }
  
  @Test
  public void adList() {
    // search ads for user1
    sessionId = login("user1", "pass");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/my-ads")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad1_1.getId().intValue(),
          ad1_2.getId().intValue()));

    // search ads for user2
    sessionId = login("user2", "pass");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/my-ads")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad2.getId().intValue()));
  }
}
