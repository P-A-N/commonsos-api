package commonsos.integration.admin.admin;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class GetAdminTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Admin com2Admin;
  private Admin com2Teller;
  private Admin nonComAdmin;
  private Admin nonComTeller;
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
  }
  
  @Test
  public void getAdmin_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // get ncl
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", ncl.getId())
      .then().statusCode(200)
      .body("id",  equalTo(ncl.getId().intValue()));

    // get com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1Admin.getId().intValue()));

    // get com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1Teller.getId().intValue()));

    // get nonComAdmin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComAdmin.getId())
      .then().statusCode(200)
      .body("id",  equalTo(nonComAdmin.getId().intValue()));

    // get nonComTeller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComTeller.getId())
      .then().statusCode(200)
      .body("id",  equalTo(nonComTeller.getId().intValue()));
  }
  
  @Test
  public void getAdmin_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    // get ncl
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", ncl.getId())
      .then().statusCode(403);

    // get com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1Admin.getId().intValue()));

    // get com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1Teller.getId().intValue()));

    // get com2Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com2Admin.getId())
      .then().statusCode(403);

    // get com2Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com2Teller.getId())
      .then().statusCode(403);

    // get nonComAdmin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComAdmin.getId())
      .then().statusCode(403);

    // get nonComTeller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComTeller.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getAdmin_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    // get ncl
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", ncl.getId())
      .then().statusCode(403);

    // get com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(403);

    // get com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(200)
      .body("id",  equalTo(com1Teller.getId().intValue()));

    // get com2Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com2Admin.getId())
      .then().statusCode(403);

    // get com2Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com2Teller.getId())
      .then().statusCode(403);

    // get nonComAdmin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComAdmin.getId())
      .then().statusCode(403);

    // get nonComTeller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComTeller.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getAdmin_byNonComAdmin() {
    sessionId = loginAdmin(nonComAdmin.getEmailAddress(), "password");
    
    // get ncl
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", ncl.getId())
      .then().statusCode(403);

    // get com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(403);

    // get com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(403);

    // get nonComAdmin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComAdmin.getId())
      .then().statusCode(200)
      .body("id",  equalTo(nonComAdmin.getId().intValue()));

    // get nonComTeller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComTeller.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getAdmin_byNonComTeller() {
    sessionId = loginAdmin(nonComTeller.getEmailAddress(), "password");
    
    // get ncl
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", ncl.getId())
      .then().statusCode(403);

    // get com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Admin.getId())
      .then().statusCode(403);

    // get com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", com1Teller.getId())
      .then().statusCode(403);

    // get nonComAdmin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComAdmin.getId())
      .then().statusCode(403);

    // get nonComTeller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins/{id}", nonComTeller.getId())
      .then().statusCode(200)
      .body("id",  equalTo(nonComTeller.getId().intValue()));
  }
}
