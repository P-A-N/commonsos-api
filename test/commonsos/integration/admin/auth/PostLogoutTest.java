package commonsos.integration.admin.auth;

import static commonsos.repository.entity.Role.NCL;
import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;

public class PostLogoutTest extends IntegrationTest {
  
  private Admin admin;
  private String sessionId;
  
  @BeforeEach
  public void createUser() throws Exception {
    admin = create(new Admin().setEmailAddress("admin@a.com").setPasswordHash(hash("pass")).setRole(NCL));
    sessionId = loginAdmin("admin@a.com", "pass");
  }
  
  @Test
  public void logout() {
    // before logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/admins/{id}", admin.getId())
    .then().statusCode(200);
    
    // logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().post("/admin/logout")
    .then().statusCode(200);
    
    // after logout
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/admins/{id}", admin.getId())
    .then().statusCode(401);
  }
}
