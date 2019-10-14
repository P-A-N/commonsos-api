package commonsos.integration.admin.admin;

import static commonsos.repository.entity.CommunityStatus.PRIVATE;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class UpdateAdminTest extends IntegrationTest {

  private Admin ncl1;
  private Admin ncl2;
  private Admin com1Admin;
  private Admin com2Admin;
  private Admin com1Teller;
  private Admin com2Teller;
  private Community com1;
  private Community com2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PRIVATE));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    
    ncl1 = create(new Admin().setEmailAddress("ncl1@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    ncl2 = create(new Admin().setEmailAddress("ncl2@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com2Admin = create(new Admin().setEmailAddress("com2Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
    com2Teller = create(new Admin().setEmailAddress("com2Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }
  
  @Test
  public void updateAdmin_ncl() throws Exception {
    sessionId = loginAdmin(ncl1.getEmailAddress(), "password");

    // update ncl1
    Map<String, Object> requestParam = getRequestParam("test adminname", "000-0000-0000", "test department");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", ncl1.getId())
      .then().statusCode(200)
      .body("adminname", equalTo("test adminname"))
      .body("telNo", equalTo("000-0000-0000"))
      .body("department", equalTo("test department"));

    // update ncl2
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", ncl2.getId())
      .then().statusCode(403);

    // update com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(200);

    // update com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateAdmin_com1Admin() throws Exception {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // update ncl1
    Map<String, Object> requestParam = getRequestParam("test adminname", "000-0000-0000", "test department");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", ncl1.getId())
      .then().statusCode(403);

    // update com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(200);

    // update com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateAdmin_com1Teller() throws Exception {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // update ncl1
    Map<String, Object> requestParam = getRequestParam("test adminname", "000-0000-0000", "test department");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", ncl1.getId())
      .then().statusCode(403);

    // update com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(403);

    // update com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(200);
  }

  private Map<String, Object> getRequestParam(String adminname, String telNo, String department) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("adminname", adminname);
    requestParam.put("telNo", telNo);
    requestParam.put("department", department);
    return requestParam;
  }
}
