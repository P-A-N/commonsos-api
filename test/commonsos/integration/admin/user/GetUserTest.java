package commonsos.integration.admin.user;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetUserTest extends IntegrationTest {

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
        .setUsername("com1User").setStatus("status").setTelNo("000").setAvatarUrl("avatar url").setEmailAddress("email address")
        .setLoggedinAt(Instant.now())
        .setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1com2User = create(new User().setUsername("com1com2User").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    nonComUser = create(new User().setUsername("nonComUser").setCommunityUserList(asList()));
  }
  
  @Test
  public void getUser_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // get com1User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1User.getId().intValue()))
      .body("username",  equalTo("com1User"))
      .body("status",  equalTo("status"))
      .body("telNo",  equalTo("000"))
      .body("avatarUrl",  equalTo("avatar url"))
      .body("emailAddress",  equalTo("email address"))
      .body("loggedinAt",  notNullValue())
      .body("createdAt",  notNullValue());

    // get com1com2User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(200)
      .body("username",  equalTo("com1com2User"));

    // get nonComUser
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(200)
      .body("username",  equalTo("nonComUser"));

    // get not exists
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", -1)
      .then().statusCode(400);
  }
  
  @Test
  public void getUser_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    // get com1User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("username",  equalTo("com1User"));

    // get com1com2User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(200)
      .body("username",  equalTo("com1com2User"));

    // get nonComUser
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    // get com1User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(200)
      .body("username",  equalTo("com1User"));

    // get com1com2User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(200)
      .body("username",  equalTo("com1com2User"));

    // get nonComUser
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byCom2Admin() {
    sessionId = loginAdmin(com2Admin.getEmailAddress(), "password");
    
    // get com1User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(403);

    // get com1com2User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(200)
      .body("username",  equalTo("com1com2User"));

    // get nonComUser
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byCom2Teller() {
    sessionId = loginAdmin(com2Teller.getEmailAddress(), "password");
    
    // get com1User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(403);

    // get com1com2User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(200)
      .body("username",  equalTo("com1com2User"));

    // get nonComUser
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byNonComAdmin() {
    sessionId = loginAdmin(nonComAdmin.getEmailAddress(), "password");
    
    // get com1User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(403);

    // get com1com2User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(403);

    // get nonComUser
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byNonComTeller() {
    sessionId = loginAdmin(nonComTeller.getEmailAddress(), "password");
    
    // get com1User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1User.getId())
      .then().statusCode(403);

    // get com1com2User
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", com1com2User.getId())
      .then().statusCode(403);

    // get nonComUser
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}", nonComUser.getId())
      .then().statusCode(403);
  }
}
