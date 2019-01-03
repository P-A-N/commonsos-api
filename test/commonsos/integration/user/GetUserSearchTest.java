package commonsos.integration.user;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class GetUserSearchTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    otherCommunity =  create(new Community().setName("otherCommunity"));
    create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    create(new User().setUsername("otherUser").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    create(new User().setUsername("otherUser2").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityList(asList(otherCommunity)));

    sessionId = login("user", "pass");
  }
  
  @Test
  public void userSearch() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users?communityId={communityId}&q={q}", community.getId(), "user")
      .then().statusCode(200)
      .body("username", contains("otherUser", "otherUser2"));
  }

  @Test
  public void userSearch_otherCommunity() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users?communityId={communityId}&q={q}", otherCommunity.getId(), "user")
      .then().statusCode(200)
      .body("username", contains("otherCommunityUser"));
  }
}
