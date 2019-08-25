package commonsos.integration.admin.admin;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class SearchAdminTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin1;
  private Admin com1Admin2;
  private Admin com1Teller1;
  private Admin com1Teller2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin1 = create(new Admin().setEmailAddress("com1Admin1@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Admin2 = create(new Admin().setEmailAddress("com1Admin2@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller1 = create(new Admin().setEmailAddress("com1Teller1@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
    com1Teller2 = create(new Admin().setEmailAddress("com1Teller2@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }
  
  @Test
  public void searchAdmin_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // search community_admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Admin1.getId().intValue(), com1Admin2.getId().intValue()));

    // search teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), TELLER.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Teller1.getId().intValue(), com1Teller2.getId().intValue()));
  }
  
  @Test
  public void searchAdmin_byCom1Admin1() {
    sessionId = loginAdmin(com1Admin1.getEmailAddress(), "password");
    
    // search community_admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Admin1.getId().intValue(), com1Admin2.getId().intValue()));

    // search teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), TELLER.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Teller1.getId().intValue(), com1Teller2.getId().intValue()));

    // search community_admin [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com2.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void searchAdmin_byCom1Teller1() {
    sessionId = loginAdmin(com1Teller1.getEmailAddress(), "password");
    
    // search community_admin [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(403);

    // search teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), TELLER.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Teller1.getId().intValue(), com1Teller2.getId().intValue()));

    // search community_admin [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com2.getId(), TELLER.getId())
      .then().statusCode(403);
  }
}
