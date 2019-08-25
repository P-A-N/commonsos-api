package commonsos.integration.app.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostUpdateUserTest extends IntegrationTest {

  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() throws Exception {
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com"));
    create(new User().setUsername("user2").setPasswordHash(hash("password")).setEmailAddress("user2@test.com"));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void updateUser() throws Exception {
    // username taken
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("firstName", "test_firstName");
    requestParam.put("lastName", "test_lastName");
    requestParam.put("description", "test_description");
    requestParam.put("location", "test_location");
    requestParam.put("telNo", "000-0000-0000");
    
    // success updating
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}", user.getId())
      .then().statusCode(200)
      .body("fullName", equalTo("test_lastName test_firstName"))
      .body("firstName", equalTo("test_firstName"))
      .body("lastName", equalTo("test_lastName"))
      .body("description", equalTo("test_description"))
      .body("location", equalTo("test_location"))
      .body("telNo", equalTo("000-0000-0000"));
  }
}
