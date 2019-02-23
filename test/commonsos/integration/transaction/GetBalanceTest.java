package commonsos.integration.transaction;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetBalanceTest extends IntegrationTest {

  private Community community;
//  private User user;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    /* user = */ create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));

    sessionId = login("user", "pass");
  }
  
  @Test
  public void balance() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/balance?communityId={communityId}", community.getId())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
  }
}
