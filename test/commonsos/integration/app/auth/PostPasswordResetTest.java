package commonsos.integration.app.auth;

import static commonsos.ApiVersion.APP_API_VERSION;
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

public class PostPasswordResetTest extends IntegrationTest {

  private User user;
  
  @BeforeEach
  public void createUser() throws Exception {
    user = create(new User().setUsername("user").setPasswordHash(hash("password1")).setEmailAddress("test@test.com"));
  }
  
  @Test
  public void passwordReset() throws Exception {
    // passwordResetRequest
    Map<String, Object> passwordResetRequestParam = getPasswordResetRequestParam();
    given()
      .body(gson.toJson(passwordResetRequestParam))
      .when().post("/app/v{v}/passwordreset", APP_API_VERSION.getMajor())
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo(user.getEmailAddress());
    String accessId = extractAccessId(messages.get(0));
    
    // passwordResetCheck
    given()
      .when().get("/app/v{v}/passwordreset/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(200);

    // passwordReset
    Map<String, Object> passwordResetParam = getPasswordResetParam();
    given()
      .body(gson.toJson(passwordResetParam))
      .when().post("/app/v{v}/passwordreset/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(200);

    // check if accessId is invalid
    given()
      .when().get("/app/v{v}/passwordreset/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(400);
    given()
      .body(gson.toJson(passwordResetParam))
      .when().post("/app/v{v}/passwordreset/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(400);
    
    // login
    Map<String, Object> loginParam = new HashMap<>();
    loginParam.put("username", "user");
    loginParam.put("password", "password1");
    given()
      .body(gson.toJson(loginParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(401);
    
    loginParam.put("password", "password2");
    given()
      .body(gson.toJson(loginParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(200);
  }
  
  private Map<String, Object> getPasswordResetRequestParam() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("emailAddress", user.getEmailAddress());
    
    return requestParam;
  }
  
  private Map<String, Object> getPasswordResetParam() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("newPassword", "password2");
    
    return requestParam;
  }
}
