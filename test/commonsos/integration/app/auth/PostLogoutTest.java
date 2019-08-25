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
    sessionId = login("user", "pass");
  }
  
  @Test
  public void logout() {
    // before logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/user")
    .then().statusCode(200);
    
    // logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().post("/logout")
    .then().statusCode(200);
    
    // after logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/user")
    .then().statusCode(401);
  }
}
