package commonsos.integration.admin.user;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetUserTransactionQrCodeTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User com1User;
  private User com2User;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com1User = create(new User().setUsername("com1User").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com2User = create(new User().setUsername("com2User").setCommunityUserList(asList(new CommunityUser().setCommunity(com2))));
  }
  
  @Test
  public void getQr_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // get com1User [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/qr?communityId={comId}", com1User.getId(), com1.getId())
      .then().statusCode(200)
      .body("url", notNullValue());
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/qr?communityId={comId}&amount=1", com1User.getId(), com1.getId())
      .then().statusCode(200)
      .body("url", notNullValue());

    // get com1User [fail. must request communityId]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/qr", com1User.getId())
      .then().statusCode(468);
  }
  
  @Test
  public void getQr_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // get com1User [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/qr?communityId={comId}", com1User.getId(), com1.getId())
      .then().statusCode(200)
      .body("url", notNullValue());

    // get com2User [fail]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/qr?communityId={comId}", com2User.getId(), com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getQr_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // get com1User [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/qr?communityId={comId}", com1User.getId(), com1.getId())
      .then().statusCode(200)
      .body("url", notNullValue());

    // get com2User [fail]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/qr?communityId={comId}", com2User.getId(), com2.getId())
      .then().statusCode(403);
  }
}
