package commonsos.integration.user;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.community.Community;
import commonsos.repository.user.User;

public class GetUserTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User admin;
  private User user1;
  private User user2;
  private User user3;
  private String sessionId;
  
  @Before
  public void setup() {
    community1 =  create(new Community().setName("community1"));
    community2 =  create(new Community().setName("community2"));
    admin = create(new User().setUsername("admin").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community1)));
    update(community1.setAdminUser(admin));
    update(community2.setAdminUser(admin));
    
    user1 = create(new User().setUsername("user1").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community1)));
    user2 = create(new User().setUsername("user2").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community2)));
    user3 = create(new User().setUsername("user3").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community1,community2)));

    sessionId = login("user1", "pass");
  }
  
  @Test
  public void getUser_own() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/user")
      .then().statusCode(200)
      .body("username",  equalTo("user1"))
      .body("communityList.name", contains("community1"));
  }
  
  @Test
  public void getUser_other_by_admin() {
    sessionId = login("admin", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}", user1.getId())
      .then().statusCode(200)
      .body("username",  equalTo("user1"))
      .body("communityList.name", contains("community1"));
  }
  
  @Test
  public void getUser_other_by_gneral() {
    sessionId = login("user2", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}", user1.getId())
      .then().statusCode(200)
      .body("username",  equalTo("user1"))
      .body("communityList", nullValue());
  }
}
