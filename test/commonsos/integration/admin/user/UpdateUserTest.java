package commonsos.integration.admin.user;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class UpdateUserTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Admin com2Admin;
  private Admin com2Teller;
  private Admin nonComAdmin;
  private Admin nonComTeller;
  private User com1User;
  private User com1com2User;
  private User nonComUser;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com2Admin = create(new Admin().setEmailAddress("com2Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com2));
    com2Teller = create(new Admin().setEmailAddress("com2Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com2));

    nonComAdmin = create(new Admin().setEmailAddress("nonComAdmin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(null));
    nonComTeller = create(new Admin().setEmailAddress("nonComTeller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(null));
    
    com1User = create(new User()
        .setUsername("com1User").setStatus("status").setTelNo("000").setAvatarUrl("avatar url").setEmailAddress("email address")
        .setLoggedinAt(Instant.now())
        .setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1com2User = create(new User().setUsername("com1com2User").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    nonComUser = create(new User().setUsername("nonComUser").setCommunityUserList(asList()));
  }
  
  @Test
  public void updateUser_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // update user
    Map<String, Object> requestParam = getRequestParam("000-0000-0000");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1User.getId().intValue()))
      .body("telNo",  equalTo("000-0000-0000"));

    // invalid telNo
    requestParam = getRequestParam("tel");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", com1User.getId())
      .then().statusCode(468);

    // user not exists
    requestParam = getRequestParam("000-0000-0000");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", -1L)
      .then().statusCode(400);
  }
  
  @Test
  public void updateUser_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // update user
    Map<String, Object> requestParam = getRequestParam("000-0000-0000");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1User.getId().intValue()))
      .body("telNo",  equalTo("000-0000-0000"));

    // update user
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1com2User.getId().intValue()))
      .body("telNo",  equalTo("000-0000-0000"));

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void updateUser_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // update user
    Map<String, Object> requestParam = getRequestParam("000-0000-0000");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1User.getId().intValue()))
      .body("telNo",  equalTo("000-0000-0000"));

    // update user
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1com2User.getId().intValue()))
      .body("telNo",  equalTo("000-0000-0000"));

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
  
  private Map<String, Object> getRequestParam(String telNo) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("telNo", telNo);
    return requestParam;
  }
}
