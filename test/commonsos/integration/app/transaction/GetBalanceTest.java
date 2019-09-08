package commonsos.integration.app.transaction;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetBalanceTest extends IntegrationTest {

  private Community community;
//  private User user;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setStatus(PUBLIC).setName("community"));
    create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));

    sessionId = loginApp("user", "pass");
  }
  
  @Test
  public void balance() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/balance?communityId={communityId}", APP_API_VERSION.getMajor(), community.getId())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
  }
}
