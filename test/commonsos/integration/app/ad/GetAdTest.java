package commonsos.integration.app.ad;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
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
  private User user;
  private Ad ad;
  private String sessionId;
  
  @BeforeEach
  public void setupData() throws Exception {
    community = create(new Community().setName("community").setStatus(PUBLIC));
    user = create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad = create(new Ad().setCreatedUserId(user.getId()).setCommunityId(community.getId()).setPoints(TEN));
    
    sessionId = loginApp("user", "pass");
  }
  
  @Test
  public void adList() {
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/ads/{id}", APP_API_VERSION.getMajor(), ad.getId())
      .then().statusCode(200)
      .body("id", equalTo(ad.getId().intValue()));
  }
}
