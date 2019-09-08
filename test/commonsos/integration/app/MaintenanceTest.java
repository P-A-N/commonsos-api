package commonsos.integration.app;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.Role.NCL;
import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;

public class MaintenanceTest extends IntegrationTest {

  private Admin ncl;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
  }
  
  @Test
  public void maintenanceTest() {
    given()
      .when().get("/app/v{v}/communities", APP_API_VERSION.getMajor())
      .then().statusCode(200);
    
    given()
      .when().get("/app/v{v}/communities", APP_API_VERSION.getMajor() - 1)
      .then().statusCode(503);
    
    given()
      .when().get("/app/v{v}/communities", APP_API_VERSION.getMajor() + 1)
      .then().statusCode(404);

    
    // update to maintenance-mode=true
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("maintenanceMode", "true");
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/system/maintenance-mode")
      .then().statusCode(200);

    
    given()
      .when().get("/app/v{v}/communities", APP_API_VERSION.getMajor())
      .then().statusCode(503);
    
    given()
      .when().get("/app/v{v}/communities", APP_API_VERSION.getMajor() - 1)
      .then().statusCode(503);
    
    given()
      .when().get("/app/v{v}/communities", APP_API_VERSION.getMajor() + 1)
      .then().statusCode(404);
  }
}
