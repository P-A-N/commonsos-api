package commonsos.integration.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.community.Community;
import commonsos.repository.user.User;

public class PostAccountCreateCompleteTest extends IntegrationTest {

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
  public void accountCreateComplete() {
    given()
      .when().post("/create-account/{accessId}", "1234567890ABCDE")
      .then().statusCode(200)
      .body("username", equalTo("test"));
  }
}
