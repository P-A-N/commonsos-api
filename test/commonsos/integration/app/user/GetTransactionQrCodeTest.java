package commonsos.integration.app.user;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class GetTransactionQrCodeTest extends IntegrationTest {

  private Community com;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com = create(new Community().setName("com").setPublishStatus(PUBLIC));
    create(new User().setUsername("user1").setPasswordHash(hash("pass")));
    sessionId = loginApp("user1", "pass");
  }
  
  @Test
  public void getTransactionQrCode() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users/1/qr", APP_API_VERSION.getMajor())
      .then().statusCode(400);
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users/1/qr?communityId={id}", APP_API_VERSION.getMajor(), com.getId())
      .then().statusCode(200)
      .body("url",  notNullValue());
  }
  
  @Test
  public void getTransactionQrCode_amount() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users/1/qr?communityId={id}&amount=1", APP_API_VERSION.getMajor(), com.getId())
      .then().statusCode(200)
      .body("url",  notNullValue());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users/1/qr?communityId={id}&amount=1.5", APP_API_VERSION.getMajor(), com.getId())
      .then().statusCode(200)
      .body("url",  notNullValue());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users/1/qr?communityId={id}&amount=0.00001", APP_API_VERSION.getMajor(), com.getId())
      .then().statusCode(200)
      .body("url",  notNullValue());
  }
}
