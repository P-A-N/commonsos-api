package commonsos.integration.transaction;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.community.Community;
import commonsos.repository.user.User;

public class GetBalanceTest extends IntegrationTest {

  private Community community;
  private User user;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityId(community.getId()));

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
