package commonsos.integration.admin.community;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class UpdateCommunityTest extends IntegrationTest {

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
    com1 =  create(new Community().setName("com1").setFee(ZERO).setDescription("description of com1").setPublishStatus(PRIVATE));
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

    // update community [normal case]
    Map<String, Object> requestParam = getRequestParam("test com name", "1", "test description", "PRIVATE", asList(com1Admin1.getId(), noneComAdmin.getId()));
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(200)
      .body("communityName", equalTo("test com name"))
      .body("transactionFee", equalTo(1))
      .body("description", equalTo("test description"))
      .body("status", equalTo("PRIVATE"))
      .body("adminList.id", contains(com1Admin1.getId().intValue(), noneComAdmin.getId().intValue()))
      .body("adminList.adminname", contains("com1Admin1", "noneComAdmin"));
    
    // verify db
    Admin c1a1 = emService.get().find(Admin.class, com1Admin1.getId());
    assertThat(c1a1.getCommunity()).isEqualTo(com1);
    Admin c1a2 = emService.get().find(Admin.class, com1Admin2.getId());
    assertThat(c1a2.getCommunity()).isNull();
    Admin nca = emService.get().find(Admin.class, noneComAdmin.getId());
    assertThat(nca.getCommunity()).isEqualTo(com1);

    // update community [delete community_admins]
    requestParam = getRequestParam("", "0", "", "PRIVATE", asList());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(200);
    
    // verify db
    emService.get().refresh(c1a1);
    assertThat(c1a1.getCommunity()).isNull();
    emService.get().refresh(c1a2);
    assertThat(c1a2.getCommunity()).isNull();
    emService.get().refresh(nca);
    assertThat(nca.getCommunity()).isNull();

    // update community [publish]
    requestParam = getRequestParam("", "0", "", "PUBLIC", asList());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(200)
      .body("status", equalTo("PUBLIC"));
    
    // update community [back to PRIVATE]
    requestParam = getRequestParam("", "0", "", "PRIVATE", asList());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(468);

    // update community [too much fee]
    requestParam = getRequestParam("", "1000", "", "PUBLIC", asList());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(400);

    // update community [negative fee]
    requestParam = getRequestParam("", "-1", "", "PUBLIC", asList());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(400);

    // update community [add other communities community_admin]
    requestParam = getRequestParam("", "0", "", "PUBLIC", asList(com2Admin1.getId()));
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(400);

    // update community [add teller]
    requestParam = getRequestParam("", "0", "", "PUBLIC", asList(com1Teller.getId()));
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(400);
  }
  
  @Test
  public void updateCommunity_com1Admin() throws Exception {
    sessionId = loginAdmin(com1Admin1.getEmailAddress(), "password");

    // update community [normal case]
    Map<String, Object> requestParam = getRequestParam("test com name", "1", "test description", "PRIVATE", asList(com1Admin1.getId(), noneComAdmin.getId()));
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(200);

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void updateCommunity_com1Teller() throws Exception {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // update community [normal case]
    Map<String, Object> requestParam = getRequestParam("test com name", "1", "test description", "PRIVATE", asList(com1Admin1.getId(), noneComAdmin.getId()));
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com1.getId())
      .then().statusCode(403);

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}", com2.getId())
      .then().statusCode(403);
  }

  private Map<String, Object> getRequestParam(String communityName, String transactionFee, String description, String status, List<Long> adminIdList) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityName", communityName);
    requestParam.put("transactionFee", transactionFee);
    requestParam.put("description", description);
    requestParam.put("status", status);
    requestParam.put("adminIdList", adminIdList);
    return requestParam;
  }
}
