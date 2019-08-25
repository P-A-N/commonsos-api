package commonsos.integration.app.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class GetTransactionQrCodeTest extends IntegrationTest {

  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    create(new User().setUsername("user1").setPasswordHash(hash("pass")).setQrCodeUrl("qrCodeUrl"));
    sessionId = login("user1", "pass");
  }
  
  @Test
  public void getTransactionQrCode() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/1/qr")
      .then().statusCode(200)
      .body("url",  equalTo("qrCodeUrl"));
  }
  
  @Test
  public void getTransactionQrCode_amount() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/1/qr?amount=1")
      .then().statusCode(200)
      .body("url",  not(equalTo("qrCodeUrl")));
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/1/qr?amount=1.5")
      .then().statusCode(200)
      .body("url",  not(equalTo("qrCodeUrl")));
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/1/qr?amount=0.00001")
      .then().statusCode(200)
      .body("url",  not(equalTo("qrCodeUrl")));
  }
}
