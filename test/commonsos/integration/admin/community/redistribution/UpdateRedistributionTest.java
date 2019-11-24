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

public class UpdateRedistributionTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User com1user1;
  private User com2user1;
  private Redistribution com1r1;
  private Redistribution com1r2;
  private Redistribution com2r1;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com1user1 = create(new User().setUsername("com1user1").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com2user1 = create(new User().setUsername("com2user1").setCommunityUserList(asList(new CommunityUser().setCommunity(com2))));

    com1r1 = create(new Redistribution().setCommunity(com1).setUser(com1user1).setRate(new BigDecimal("5")));
    com1r2 = create(new Redistribution().setCommunity(com1).setUser(com1user1).setRate(new BigDecimal("90")));
    com2r1 = create(new Redistribution().setCommunity(com2).setUser(com1user1).setRate(new BigDecimal("1.5")));
  }
  
  @Test
  public void updateRedistribution_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // update redistribution [failed. rate is grater than 100]
    Map<String, Object> requestParam = getRequestParam("true", null, "11");
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}", com1.getId(), com1r1.getId())
      .then().statusCode(400);

    // update redistribution [success]
    requestParam = getRequestParam("true", null, "10");
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}", com1.getId(), com1r1.getId())
      .then().statusCode(200)
      .body("isAll", equalTo(true))
      .body("userId", nullValue())
      .body("username", nullValue())
      .body("redistributionRate", equalTo(10));

    // update redistribution [failed. user is not a community member]
    requestParam = getRequestParam("false", com2user1.getId(), "10");
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}", com1.getId(), com1r1.getId())
      .then().statusCode(400);
  }
  
  @Test
  public void updateRedistribution_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // update redistribution
    Map<String, Object> requestParam = getRequestParam("true", null, "10");
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}", com1.getId(), com1r1.getId())
      .then().statusCode(200);
    
    // update redistribution [forbidden]
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}", com2.getId(), com2r1.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void updateRedistribution_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // update redistribution
    Map<String, Object> requestParam = getRequestParam("true", null, "10");
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}", com1.getId(), com1r1.getId())
      .then().statusCode(403);
    
    // update redistribution [forbidden]
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}", com2.getId(), com2r1.getId())
      .then().statusCode(403);
  }

  private Map<String, Object> getRequestParam(String isAll, Long userId, String redistributionRate) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("isAll", isAll);
    requestParam.put("userId", userId);
    requestParam.put("redistributionRate", redistributionRate);
    return requestParam;
  }
}
