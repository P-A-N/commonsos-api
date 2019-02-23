package commonsos.integration.ad;

import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetAdTest extends IntegrationTest {

  private Community community;
  private User user;
  private Ad ad;
  private String sessionId;
  
  @Before
  public void setupData() {
    community = create(new Community().setName("community"));
    user = create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad = create(new Ad().setCreatedBy(user.getId()).setCommunityId(community.getId()).setPoints(TEN));
    
    sessionId = login("user", "pass");
  }
  
  @Test
  public void adList() {
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/ads/{id}", ad.getId())
      .then().statusCode(200)
      .body("id", equalTo(ad.getId().intValue()));
  }
}
