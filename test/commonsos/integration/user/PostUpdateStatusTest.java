package commonsos.integration.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostUpdateStatusTest extends IntegrationTest {

  private User user;
  private String sessionId;
  
  @Before
  public void createUser() {
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com"));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void updateUsername() throws Exception {
    // success updating
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("status", "hoge");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/status", user.getId())
      .then().statusCode(200)
      .body("status", equalTo("hoge"));
  }
}
