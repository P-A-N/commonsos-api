package commonsos.integration.app.user;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class PostUpdateCommunitiesTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  private User admin1;
  private User admin2;
  private User admin3;
  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() throws Exception {
    community1 = create(new Community().setStatus(PUBLIC).setName("community1").setTokenContractAddress("0x0"));
    community2 = create(new Community().setStatus(PUBLIC).setName("community2").setTokenContractAddress("0x0"));
    community3 = create(new Community().setStatus(PUBLIC).setName("community3").setTokenContractAddress("0x0"));
    admin1 = create(new User().setUsername("admin1").setPasswordHash(hash("password")).setEmailAddress("admin1@test.com").setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    admin2 = create(new User().setUsername("admin2").setPasswordHash(hash("password")).setEmailAddress("admin2@test.com").setCommunityUserList(asList(new CommunityUser().setCommunity(community2))));
    admin3 = create(new User().setUsername("admin3").setPasswordHash(hash("password")).setEmailAddress("admin3@test.com").setCommunityUserList(asList(new CommunityUser().setCommunity(community3))));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1).setWalletLastViewTime(Instant.EPOCH.plus(1, ChronoUnit.DAYS)),
        new CommunityUser().setCommunity(community2).setWalletLastViewTime(Instant.EPOCH.plus(1, ChronoUnit.DAYS)))));
    update(community1.setAdminUser(admin1));
    update(community2.setAdminUser(admin2));
    update(community3.setAdminUser(admin3));
    
    sessionId = loginApp("user", "password");
  }
  
  @Test
  public void updateCommunities() throws Exception {
    Map<String, Object> requestParam = new HashMap<>();
    List<Long> communityList = new ArrayList<>(Arrays.asList(community1.getId()));
    requestParam.put("communityList", communityList);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v99/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(1))
      .body("communityList.id", contains(community1.getId().intValue()))
      .body("communityList.walletLastViewTime", contains(Instant.EPOCH.plus(1, ChronoUnit.DAYS).toString()));
    
    communityList = new ArrayList<>(Arrays.asList(community1.getId(), community3.getId()));
    requestParam.put("communityList", communityList);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v99/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(2))
      .body("communityList.id", contains(community1.getId().intValue(), community3.getId().intValue()))
      .body("communityList.name", contains(community1.getName(), community3.getName()))
      .body("communityList.walletLastViewTime", contains(Instant.EPOCH.plus(1, ChronoUnit.DAYS).toString(), Instant.EPOCH.toString()));

    communityList = new ArrayList<>(Arrays.asList(community2.getId(), community3.getId()));
    requestParam.put("communityList", communityList);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v99/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(2))
      .body("communityList.id", contains(community2.getId().intValue(), community3.getId().intValue()))
      .body("communityList.name", contains(community2.getName(), community3.getName()))
      .body("communityList.walletLastViewTime", contains(Instant.EPOCH.toString(), Instant.EPOCH.toString()));
  }
}
