package commonsos.integration.admin.community;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class UpdateCommunityTokenNameTest extends IntegrationTest {

  private Admin ncl;
  private Admin com1Admin1;
  private Admin com1Admin2;
  private Admin com2Admin1;
  private Admin noneComAdmin;
  private Admin com1Teller;
  private Community com1;
  private Community com2;
  private String sessionId;

  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PRIVATE));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setAdminname("ncl").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin1 = create(new Admin().setEmailAddress("com1Admin1@before.each.com").setAdminname("com1Admin1").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Admin2 = create(new Admin().setEmailAddress("com1Admin2@before.each.com").setAdminname("com1Admin2").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com2Admin1 = create(new Admin().setEmailAddress("com2Admin1@before.each.com").setAdminname("com2Admin1").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com2));
    noneComAdmin = create(new Admin().setEmailAddress("noneComAdmin@before.each.com").setAdminname("noneComAdmin").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setAdminname("com1Teller").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }
  
  @Test
  public void updateCommunity_ncl() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // update community tokenName
    Map<String, Object> requestParam = getRequestParam("newTokenName");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}/tokenName", com1.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateCommunity_com1Admin() throws Exception {
    sessionId = loginAdmin(com1Admin1.getEmailAddress(), "password");

    // update community tokenName
    Map<String, Object> requestParam = getRequestParam("newTokenName");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}/tokenName", com1.getId())
      .then().statusCode(200);

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}/tokenName", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void updateCommunity_com1Teller() throws Exception {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // update community tokenName
    Map<String, Object> requestParam = getRequestParam("newTokenName");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}/tokenName", com1.getId())
      .then().statusCode(403);

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}/tokenName", com2.getId())
      .then().statusCode(403);
  }

  private Map<String, Object> getRequestParam(String tokenName) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("tokenName", tokenName);
    return requestParam;
  }
}
