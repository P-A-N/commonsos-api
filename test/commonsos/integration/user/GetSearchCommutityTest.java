package commonsos.integration.user;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetSearchCommutityTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  private Community community4;
  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() {
    community1 = create(new Community().setName("comm_foo").setTokenContractAddress("0x0"));
    community2 = create(new Community().setName("comm_foo_bar").setTokenContractAddress("0x0"));
    community3 = create(new Community().setName("comm_bar").setTokenContractAddress("0x0"));
    community4 = create(new Community().setName("comm_bar_foo").setTokenContractAddress("0x0"));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4))));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void searchCommutity() throws Exception {
    // non filter
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(3))
      .body("communityList.id", contains(community2.getId().intValue(), community3.getId().intValue(), community4.getId().intValue()))
      .body("communityList.walletLastViewTime", contains(Instant.EPOCH.toString(), Instant.EPOCH.toString(), Instant.EPOCH.toString()));

    // filter
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?filter={filter}", user.getId(), "foo")
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(2))
      .body("communityList.id", contains(community2.getId().intValue(), community4.getId().intValue()))
      .body("communityList.walletLastViewTime", contains(Instant.EPOCH.toString(), Instant.EPOCH.toString()));
  }
}
