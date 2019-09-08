package commonsos.integration.app;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;

public class GetAppApiVersionTest extends IntegrationTest {
  
  @Test
  public void getVersion() {
    given()
      .when().get("/app/version")
      .then().statusCode(200)
      .body("apiVersion", equalTo("2.0.0"));
  }
}
