package commonsos.integration.user;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class GetSearchCommutityTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  private Community community4;
  private User user;
  private String sessionId;
  
  @Before
  public void createUser() {
    community1 = create(new Community().setName("comm_foo").setTokenContractAddress("0x0"));
    community2 = create(new Community().setName("comm_foo_bar").setTokenContractAddress("0x0"));
    community3 = create(new Community().setName("comm_bar").setTokenContractAddress("0x0"));
    community4 = create(new Community().setName("comm_bar_foo").setTokenContractAddress("0x0"));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com")
        .setCommunityList(asList(community2,community3,community4)));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void searchCommutity() throws Exception {
    // non filter
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("id", iterableWithSize(3))
      .body("id", contains(community2.getId().intValue(), community3.getId().intValue(), community4.getId().intValue()));

    // filter
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?filter={filter}", user.getId(), "foo")
      .then().statusCode(200)
      .body("id", iterableWithSize(2))
      .body("id", contains(community2.getId().intValue(), community4.getId().intValue()));
  }
}
