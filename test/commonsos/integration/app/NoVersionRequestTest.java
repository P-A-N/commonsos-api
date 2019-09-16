package commonsos.integration.app;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;

public class NoVersionRequestTest extends IntegrationTest {
  
  @Test
  public void noVersion() {
    given()
      .when().post("/login")
      .then().statusCode(503);
    
    given()
      .when().post("/logout")
    .then().statusCode(503);
    
    given()
      .when().post("/create-account")
      .then().statusCode(503);
    
    given()
      .when().post("/create-account/12345")
      .then().statusCode(503);
    
    given()
      .when().get("/user")
      .then().statusCode(503);
    
    given()
      .when().get("/users")
      .then().statusCode(503);
    
    given()
      .when().post("/users/1")
      .then().statusCode(503);
    
    given()
      .when().post("/users/1/username")
      .then().statusCode(503);
    
    given()
      .when().get("/users/1/qr")
      .then().statusCode(503);
    
    given()
      .when().get("/ads")
      .then().statusCode(503);
    
    given()
      .when().post("/ads")
      .then().statusCode(503);
    
    given()
      .when().get("/ads/1")
      .then().statusCode(503);
    
    given()
      .when().post("/ads/1")
      .then().statusCode(503);
    
    given()
      .when().post("/ads/1/photo")
      .then().statusCode(503);
    
    given()
      .when().get("/my-ads")
      .then().statusCode(503);
    
    given()
      .when().get("/balance")
      .then().statusCode(503);
    
    given()
      .when().get("/transactions")
      .then().statusCode(503);
    
    given()
      .when().post("/transactions")
      .then().statusCode(503);
    
    given()
      .when().get("/message-threads")
      .then().statusCode(503);
    
    given()
      .when().get("/message-threads/1")
      .then().statusCode(503);
    
    given()
      .when().get("/message-threads/1/messages")
      .then().statusCode(503);
    
    given()
      .when().post("/message-threads/group")
      .then().statusCode(503);
    
    given()
      .when().post("/message-threads/1/group")
      .then().statusCode(503);
    
    given()
      .when().get("/communities")
      .then().statusCode(503);
    
    given()
      .when().get("/communities/1/notification")
      .then().statusCode(503);
    
    given()
      .when().post("/communities/1/photo")
      .then().statusCode(503);
  }
}
