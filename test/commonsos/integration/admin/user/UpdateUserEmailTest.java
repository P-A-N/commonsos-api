package commonsos.integration.admin.user;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class UpdateUserEmailTest extends IntegrationTest {

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
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com2Admin = create(new Admin().setEmailAddress("com2Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com2));
    com2Teller = create(new Admin().setEmailAddress("com2Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com2));

    nonComAdmin = create(new Admin().setEmailAddress("nonComAdmin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(null));
    nonComTeller = create(new Admin().setEmailAddress("nonComTeller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(null));
    
    com1User = create(new User()
        .setUsername("com1User").setStatus("status").setTelNo("000").setEmailAddress("com1User@test.com")
        .setPasswordHash(hash("password"))
        .setLoggedinAt(Instant.now())
        .setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1com2User = create(new User().setUsername("com1com2User").setEmailAddress("com1com2User@test.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    nonComUser = create(new User().setUsername("nonComUser").setEmailAddress("nonComUser@test.com").setCommunityUserList(asList()));
  }
  
  @Test
  public void updateUser_byNcl() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // update user
    Map<String, Object> requestParam = getRequestParam("com1User.test@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", com1User.getId())
      .then().statusCode(200);

    // check email address is not updated yet
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("emailAddress", equalTo("com1User@test.com"));
    
    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("com1User.test@test.com");
    String accessId = extractAccessId(messages.get(0));
    
    // update completely by user
    given()
      .when().post("/app/v{v}/users/{id}/emailaddress/{accessId}", APP_API_VERSION.getMajor(), com1User.getId(), accessId)
      .then().statusCode(200);

    // check email address is updated
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("emailAddress", equalTo("com1User.test@test.com"));
    
    // invalid email
    requestParam = getRequestParam("invalid");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", com1User.getId())
      .then().statusCode(400);

    // email address taken
    requestParam = getRequestParam(com1com2User.getEmailAddress());
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", com1User.getId())
      .then().statusCode(468);

    // user not exists
    requestParam = getRequestParam("000-0000-0000");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", -1L)
      .then().statusCode(400);
  }
  
  @Test
  public void updateUser_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // update user
    Map<String, Object> requestParam = getRequestParam("com1User.test@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", com1User.getId())
      .then().statusCode(200);

    // update user
    requestParam = getRequestParam("com1com2User.test@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", com1com2User.getId())
      .then().statusCode(200);

    // forbidden
    requestParam = getRequestParam("nonComUser.test@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void updateUser_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // update user
    Map<String, Object> requestParam = getRequestParam("com1User.test@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", com1User.getId())
      .then().statusCode(200);

    // update user
    requestParam = getRequestParam("com1com2User.test@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", com1com2User.getId())
      .then().statusCode(200);

    // forbidden
    requestParam = getRequestParam("nonComUser.test@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/users/{id}/emailaddress", nonComUser.getId())
      .then().statusCode(403);
  }
  
  private Map<String, Object> getRequestParam(String newEmailAddress) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("newEmailAddress", newEmailAddress);
    return requestParam;
  }
}
