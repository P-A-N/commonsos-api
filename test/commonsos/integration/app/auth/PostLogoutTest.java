package commonsos.integration.app.auth;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostLogoutTest extends IntegrationTest {
  
  private String sessionId;
  
  @BeforeEach
  public void createUser() throws Exception {
    create(new User().setUsername("user").setPasswordHash(hash("pass")));
    sessionId = loginApp("user", "pass");
  }
  
  @Test
  public void logout() {
    // before logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/app/v99/user")
    .then().statusCode(200);
    
    // logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().post("/app/v99/logout")
    .then().statusCode(200);
    
    // after logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/app/v99/user")
    .then().statusCode(401);
  }
}
