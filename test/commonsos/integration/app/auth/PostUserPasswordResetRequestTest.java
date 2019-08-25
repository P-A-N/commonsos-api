package commonsos.integration.app.auth;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostUserPasswordResetRequestTest extends IntegrationTest {

  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() throws Exception {
    user = create(new User().setUsername("user").setPasswordHash(hash("old_password")).setEmailAddress("user@test.com"));
    
    sessionId = login("user", "old_password");
  }
  
  @Test
  public void postUserPasswordResetRequest() throws Exception {
    // password reset request
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("currentPassword", "old_password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/passwordreset", user.getId())
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo(user.getEmailAddress());
    String accessId = extractAccessId(messages.get(0));
    
    // password isn't reset yet
    failLogin("user", "new_password");

    // passwordResetCheck
    given()
      .when().get("/passwordreset/{accessId}", accessId)
      .then().statusCode(200);

    // passwordReset
    requestParam = new HashMap<>();
    requestParam.put("newPassword", "new_password");
    given()
      .body(gson.toJson(requestParam))
      .when().post("/passwordreset/{accessId}", accessId)
      .then().statusCode(200);

    // password is reset
    login("user", "new_password");
    failLogin("user", "old_password");

    // passwordResetCheck invalid
    given()
      .when().get("/passwordreset/{accessId}", accessId)
      .then().statusCode(400);
  }

  @Test
  public void postUserPasswordResetRequest_password_incorrect() throws Exception {
    // password reset request
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("currentPassword", "incorrect_password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/passwordreset", user.getId())
      .then().statusCode(401);
  }
}
