package commonsos.integration.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.integration.IntegrationTest;
import commonsos.repository.user.User;

@RunWith(MockitoJUnitRunner.class)
public class LoginTest extends IntegrationTest {
  
  @Before
  public void createUser() {
    create(new User().setUsername("user").setPasswordHash(passwordService.hash("pass")));
  }
  
  @Test
  public void login_success() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "user");
    requestParam.put("password", "pass");
    
    given()
      .body(gson.toJson(requestParam))
      .when()
      .post("/login")
      .then()
      .statusCode(200)
      .body("username", equalTo("user"));
  }
  
  @Test
  public void login_failed() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "invalid user");
    requestParam.put("password", "invalid pass");
    
    given()
      .body(gson.toJson(requestParam))
      .when()
      .post("/login")
      .then()
      .statusCode(401);
  }
}
