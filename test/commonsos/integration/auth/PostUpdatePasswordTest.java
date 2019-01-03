package commonsos.integration.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostUpdatePasswordTest extends IntegrationTest {

  private User user;
  private String sessionId;
  
  @Before
  public void createUser() {
    user = create(new User().setUsername("user").setPasswordHash(hash("old_password")).setEmailAddress("user@test.com"));
    
    sessionId = login("user", "old_password");
  }
  
  @Test
  public void updateUsername() throws Exception {
    // success updating
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("password", "new_password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/password", user.getId())
      .then().statusCode(200)
      .body("username", equalTo("user"));
    
    // success login with new password
    login("user", "new_password");
    
    // failed login with old password
    failLogin("user", "old_password");
  }
}
