package commonsos.integration.admin.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;

public class PostLoginTest extends IntegrationTest {
  
  @BeforeEach
  public void createUser() {
    create(new Admin().setEmailAddress("admin@a.com").setPasswordHash(hash("pass")));
  }
  
  @Test
  public void login_success() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("emailAddress", "admin@a.com");
    requestParam.put("password", "pass");
    
    String loggedinAt = given()
      .body(gson.toJson(requestParam))
      .when().post("/admin/login")
      .then().statusCode(200)
      .body("emailAddress", equalTo("admin@a.com"))
      .extract().path("loggedinAt");
    
    Instant loggedinAt1 = Instant.parse(loggedinAt);

    loggedinAt = given()
      .body(gson.toJson(requestParam))
      .when().post("/admin/login")
      .then().statusCode(200)
      .extract().path("loggedinAt");
    
    Instant loggedinAt2 = Instant.parse(loggedinAt);
    
    assertTrue(loggedinAt1.isBefore(loggedinAt2));
  }
  
  @Test
  public void login_failed() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("emailAddress", "invalidAdmin@a.com");
    requestParam.put("password", "invalid pass");
    
    given()
      .body(gson.toJson(requestParam))
      .when().post("/admin/login")
      .then().statusCode(401);
  }
}
