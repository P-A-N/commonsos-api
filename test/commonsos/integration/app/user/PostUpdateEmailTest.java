package commonsos.integration.app.user;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostUpdateEmailTest extends IntegrationTest {

  private User user;
  private User user2;
  private String sessionId;
  private String sessionId2;
  
  @BeforeEach
  public void createUser() {
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com"));
    user2 = create(new User().setUsername("user2").setPasswordHash(hash("password")).setEmailAddress("user2@test.com"));
  }
  
  @Test
  public void updateEmail() throws Exception {
    // it should fail before login
    Map<String, Object> updateEmailTemporaryParam = getUpdateEmailTemporaryParam();
    given()
      .body(gson.toJson(updateEmailTemporaryParam))
      .when().post("/users/{:id}/emailaddress", user.getId())
      .then().statusCode(401);
    
    // it should success after login
    sessionId = login("user", "password");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(updateEmailTemporaryParam))
      .when().post("/users/{id}/emailaddress", user.getId())
      .then().statusCode(200);
    
    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("updated@test.com");
    String accessId = extractAccessId(messages.get(0));
    
    // email address is taken
    Map<String, Object> createAccountParam = getCreateAccountParam();
    createAccountParam.put("emailAddress", "user@test.com");
    given()
      .body(gson.toJson(createAccountParam))
      .when().post("/create-account")
      .then().statusCode(468);
    createAccountParam.put("emailAddress", "updated@test.com");
    given()
      .body(gson.toJson(createAccountParam))
      .when().post("/create-account")
      .then().statusCode(468);
    sessionId2 = login("user2", "password");
    given()
      .cookie("JSESSIONID", sessionId2)
      .body(gson.toJson(updateEmailTemporaryParam))
      .when().post("/users/{id}/emailaddress", user2.getId())
      .then().statusCode(468);
    
    // updateEmail complete
    given()
      .when().post("/users/{id}/emailaddress/{accessId}", user.getId(), accessId)
      .then().statusCode(200);
    
    // check email address is updated
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/user")
      .then().statusCode(200)
      .body("emailAddress",  equalTo("updated@test.com"));

    // check if accessId is invalid
    given()
      .when().post("/users/{id}/emailaddress/{accessId}", user.getId(), accessId)
      .then().statusCode(400);
  }
  
  private Map<String, Object> getUpdateEmailTemporaryParam() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("newEmailAddress", "updated@test.com");
    
    return requestParam;
  }
  
  private Map<String, Object> getCreateAccountParam() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "user");
    requestParam.put("password", "password");
    
    return requestParam;
  }
}
