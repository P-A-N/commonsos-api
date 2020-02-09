package commonsos.integration.admin.community.redistribution;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.User;

public class CreateRedistributionTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User user1;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
    
    user1 = create(new User().setUsername("user1").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
  }
  
  @Test
  public void createRedistribution_ncl() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // prepare
    Map<String, Object> reqParam = new HashMap<>();
    reqParam.put("isAll", false);
    reqParam.put("userId", user1.getId());
    reqParam.put("redistributionRate", "1.5");

    // create redistribution [success]
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(200)
      .body("isAll", equalTo(false))
      .body("userId", equalTo(user1.getId().intValue()))
      .body("username", equalTo("user1"))
      .body("redistributionRate", equalTo(1.5F));

    // create redistribution [not a member]
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com2.getId())
      .then().statusCode(468);

    // create redistribution [user is not specified]
    reqParam.put("userId", null);
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(468);

    // create redistribution [success with allUser]
    reqParam.put("isAll", true);
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(200)
      .body("isAll", equalTo(true))
      .body("userId", nullValue())
      .body("redistributionRate", equalTo(1.5F));

    // create redistribution [community not exists]
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", -1)
      .then().statusCode(400);

    // create redistribution [rate is 0]
    reqParam.put("redistributionRate", "0");
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(468);

    // create redistribution [too much rate]
    reqParam.put("redistributionRate", "1.5");
    create(new Redistribution().setCommunity(com1).setAll(true).setRate(new BigDecimal("100")));
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(468);
  }
  
  @Test
  public void createRedistribution_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    // prepare
    Map<String, Object> reqParam = new HashMap<>();
    reqParam.put("isAll", true);
    reqParam.put("redistributionRate", "1.5");

    // create redistribution [success]
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(200)
      .body("isAll", equalTo(true))
      .body("redistributionRate", equalTo(1.5F));

    // create redistribution [not a admin of community]
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void createRedistribution_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    // prepare
    Map<String, Object> reqParam = new HashMap<>();
    reqParam.put("isAll", true);
    reqParam.put("redistributionRate", "1.5");

    // create redistribution [forbidden]
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(403);

    // create redistribution [not a admin of community]
    given()
      .body(gson.toJson(reqParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions", com2.getId())
      .then().statusCode(403);
  }
}
