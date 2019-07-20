package commonsos.integration.app.user;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class PostUpdateLastViewTimeTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community otherCommunity;
  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() {
    community1 = create(new Community().setName("community1").setTokenContractAddress("0x0"));
    community2 = create(new Community().setName("community2").setTokenContractAddress("0x0"));
    otherCommunity = create(new Community().setName("otherCommunity").setTokenContractAddress("0x0"));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2))));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void updateLastViewTime() throws Exception {
    // check last view time before testing
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/user")
      .then().statusCode(200)
      .body("communityList.walletLastViewTime", contains(
          is(Instant.EPOCH.toString()),
          is(Instant.EPOCH.toString())))
      .body("communityList.adLastViewTime", contains(
          is(Instant.EPOCH.toString()),
          is(Instant.EPOCH.toString())))
      .body("communityList.notificationLastViewTime", contains(
          is(Instant.EPOCH.toString()),
          is(Instant.EPOCH.toString())));
    
    // update wallet LastViewTime
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community1.getId());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/wallet/lastViewTime", user.getId())
      .then().statusCode(200);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/user")
      .then().statusCode(200)
      .body("communityList.walletLastViewTime", contains(
          not(is(Instant.EPOCH.toString())),
          is(Instant.EPOCH.toString())))
      .body("communityList.adLastViewTime", contains(
          is(Instant.EPOCH.toString()),
          is(Instant.EPOCH.toString())))
      .body("communityList.notificationLastViewTime", contains(
          is(Instant.EPOCH.toString()),
          is(Instant.EPOCH.toString())));

    // update ad LastViewTime
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/ad/lastViewTime", user.getId())
      .then().statusCode(200);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/user")
      .then().statusCode(200)
      .body("communityList.walletLastViewTime", contains(
          not(is(Instant.EPOCH.toString())),
          is(Instant.EPOCH.toString())))
      .body("communityList.adLastViewTime", contains(
          not(is(Instant.EPOCH.toString())),
          is(Instant.EPOCH.toString())))
      .body("communityList.notificationLastViewTime", contains(
          is(Instant.EPOCH.toString()),
          is(Instant.EPOCH.toString())));

    // update notification LastViewTime
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/notification/lastViewTime", user.getId())
      .then().statusCode(200);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/user")
      .then().statusCode(200)
      .body("communityList.walletLastViewTime", contains(
          not(is(Instant.EPOCH.toString())),
          is(Instant.EPOCH.toString())))
      .body("communityList.adLastViewTime", contains(
          not(is(Instant.EPOCH.toString())),
          is(Instant.EPOCH.toString())))
      .body("communityList.notificationLastViewTime", contains(
          not(is(Instant.EPOCH.toString())),
          is(Instant.EPOCH.toString())));
  }

  
  @Test
  public void updateLastViewTime_notMember() throws Exception {
    // update wallet LastViewTime
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", otherCommunity.getId());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/wallet/lastViewTime", user.getId())
      .then().statusCode(400);

    // update ad LastViewTime
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/ad/lastViewTime", user.getId())
      .then().statusCode(400);

    // update notification LastViewTime
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/notification/lastViewTime", user.getId())
      .then().statusCode(400);
  }
}
