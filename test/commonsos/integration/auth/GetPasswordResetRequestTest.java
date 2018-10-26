package commonsos.integration.auth;

import static io.restassured.RestAssured.given;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class GetPasswordResetRequestTest extends IntegrationTest {

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
  public void passwordResetRequest() {
    given()
      .when().get("/passwordreset/{accessId}", "asdfg")
      .then().statusCode(200);
  }
}
