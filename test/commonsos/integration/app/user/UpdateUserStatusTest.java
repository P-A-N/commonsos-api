package commonsos.integration.app.user;

import static commonsos.ApiVersion.APP_API_VERSION;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class UpdateUserStatusTest extends IntegrationTest {

  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() throws Exception {
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com"));
    
    sessionId = loginApp("user", "password");
  }
  
  @Test
  public void updateUsername() throws Exception {
    // success updating
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("status", "hoge");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/users/{id}/status", APP_API_VERSION.getMajor(), user.getId())
      .then().statusCode(200)
      .body("status", equalTo("hoge"));
  }
}
