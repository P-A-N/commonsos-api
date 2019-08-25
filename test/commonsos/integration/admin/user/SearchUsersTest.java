package commonsos.integration.admin.user;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class SearchUsersTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Admin com2Admin;
  private Admin com2Teller;
  private Admin nonComAdmin;
  private Admin nonComTeller;
  private User com1User1;
  private User com1User2;
  private User com1User3;
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
    
    com1User1 = create(new User().setUsername("com1User1").setEmailAddress("com1User1@a.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1User2 = create(new User().setUsername("com1User2").setEmailAddress("com1User2@b.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1User3 = create(new User().setUsername("com1User3").setEmailAddress("com1User3@c.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1com2User = create(new User().setUsername("com1com2User").setEmailAddress("com1com2User@d.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    nonComUser = create(new User().setUsername("nonComUser").setEmailAddress("nonComUser@e.com").setCommunityUserList(asList()));
  }
  
  @Test
  public void getUser_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users")
      .then().statusCode(200)
      .body("userList.username",  contains("com1User1", "com1User2", "com1User3", "com1com2User", "nonComUser"));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?username={username}", "2")
      .then().statusCode(200)
      .body("userList.username",  contains("com1User2", "com1com2User"));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?emailAddress={e}", "a.com")
      .then().statusCode(200)
      .body("userList.username",  contains("com1User1"));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("userList.username",  contains("com1User1", "com1User2", "com1User3", "com1com2User"));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?username={name}&emailAddress={address}&communityId={id}", "com1", "user", com2.getId())
      .then().statusCode(200)
      .body("userList.username",  contains("com1com2User"));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?username={name}", "invalid")
      .then().statusCode(200)
      .body("userList.username",  empty());
  }
  
  @Test
  public void getUser_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users")
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("userList.username",  contains("com1User1", "com1User2", "com1User3", "com1com2User"));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?communityId={id}", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users")
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("userList.username",  contains("com1User1", "com1User2", "com1User3", "com1com2User"));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?communityId={id}", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byNonComAdmin() {
    sessionId = loginAdmin(nonComAdmin.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users")
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?communityId={id}", com1.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byNonComTeller() {
    sessionId = loginAdmin(nonComTeller.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users")
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?communityId={id}", com1.getId())
      .then().statusCode(403);
  }

  @Test
  public void searchCommunity_pagination() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    create(new User().setUsername("pu1"));
    create(new User().setUsername("pu2"));
    create(new User().setUsername("pu3"));
    create(new User().setUsername("pu4"));
    create(new User().setUsername("pu5"));
    create(new User().setUsername("pu6"));
    create(new User().setUsername("pu7"));
    create(new User().setUsername("pu8"));
    create(new User().setUsername("pu9"));
    create(new User().setUsername("pu10"));
    create(new User().setUsername("pu11"));
    create(new User().setUsername("pu12"));

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?username={name}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "pu", "0", "10", "ASC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "pu1", "pu2", "pu3", "pu4", "pu5",
          "pu6", "pu7", "pu8", "pu9", "pu10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?username={name}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "pu", "1", "10", "ASC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "pu11", "pu12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?username={name}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "pu", "0", "10", "DESC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "pu12", "pu11", "pu10", "pu9", "pu8",
          "pu7", "pu6", "pu5", "pu4", "pu3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users?username={name}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "pu", "1", "10", "DESC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "pu2", "pu1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
