package commonsos.integration.app.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostUpdateUsernameTest extends IntegrationTest {

  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() throws Exception {
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com"));
    create(new User().setUsername("user2").setPasswordHash(hash("password")).setEmailAddress("user2@test.com"));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void updateUsername() throws Exception {
    // username taken
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "user2");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/username", user.getId())
      .then().statusCode(468)
      .body("key", equalTo("error.usernameTaken"));
    
    // success updating
    requestParam.put("username", "user_updated");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/username", user.getId())
      .then().statusCode(200)
      .body("username", equalTo("user_updated"));
    
    // success login with new username
    login("user_updated", "password");
    
    // fail login with old username
    failLogin("user", "password");
  }
}
