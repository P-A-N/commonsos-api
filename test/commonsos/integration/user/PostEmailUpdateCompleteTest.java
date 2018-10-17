package commonsos.integration.user;

import static io.restassured.RestAssured.given;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.community.Community;
import commonsos.repository.user.User;

public class PostEmailUpdateCompleteTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  private User admin;
  
  @Before
  public void createUser() {
    admin = create(new User().setUsername("admin"));
    community1 =  create(new Community().setName("community1").setAdminUser(admin));
    community2 =  create(new Community().setName("community2").setAdminUser(admin));
    community3 =  create(new Community().setName("community3").setAdminUser(admin));
  }
  
  @Test
  public void provisionalAccountCreate() {
    given()
      .when().post("/users/{id}/emailaddress/{accessId}", 123, "abcdefg")
      .then().statusCode(200);
  }
}
