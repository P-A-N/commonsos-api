package commonsos.integration.app.user;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetUserTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User admin;
  private User user1;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community1 =  create(new Community().setPublishStatus(PUBLIC).setName("community1"));
    community2 =  create(new Community().setPublishStatus(PUBLIC).setName("community2"));
    admin = create(new User().setUsername("admin").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    update(community1.setAdminUser(admin));
    update(community2.setAdminUser(admin));
    
    user1 = create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community2))));
    create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2))));

    sessionId = loginApp("user1", "pass");
  }
  
  @Test
  public void getUser_own() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/user", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("username",  equalTo("user1"))
      .body("communityList.name", contains("community1"))
      .body("communityList.balance", contains(10))
      .body("communityList.walletLastViewTime", contains(notNullValue()));
  }
  
  @Test
  public void getUser_other_by_admin() {
    sessionId = loginApp("admin", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users/{id}", APP_API_VERSION.getMajor(), user1.getId())
      .then().statusCode(200)
      .body("username",  equalTo("user1"))
      .body("communityList.name", contains("community1"))
      .body("communityList.walletLastViewTime", contains(notNullValue()));
  }
  
  @Test
  public void getUser_other_by_gneral() {
    sessionId = loginApp("user2", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users/{id}", APP_API_VERSION.getMajor(), user1.getId())
      .then().statusCode(200)
      .body("username",  equalTo("user1"))
      .body("communityList.name", contains("community1"))
      .body("communityList.walletLastViewTime", contains(nullValue()));
  }
}
