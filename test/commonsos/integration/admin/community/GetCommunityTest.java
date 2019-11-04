package commonsos.integration.admin.community;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetCommunityTest extends IntegrationTest {

  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Community publicCom;
  private Community privateCom;
  private Community deleteCom;
  private Community otherCom;
  private Admin publicComAdmin;
  private Admin publicComTeller;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    publicCom =  create(new Community().setName("publicCom").setPublishStatus(PUBLIC).setFee(BigDecimal.TEN).setDescription("des").setAdminPageUrl("url"));
    privateCom =  create(new Community().setName("privateCom").setPublishStatus(PRIVATE));
    deleteCom =  create(new Community().setName("deleteCom").setPublishStatus(PUBLIC).setDeleted(true));
    otherCom =  create(new Community().setName("otherCom").setPublishStatus(PUBLIC));
    
    // create admins
    publicComAdmin = create(new Admin().setEmailAddress("publicComAdmin@before.each.com").setAdminname("publicComAdmin").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(publicCom));
    publicComTeller = create(new Admin().setEmailAddress("publicComTeller@before.each.com").setAdminname("publicComTeller").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(publicCom));

    // create users
    create(new User().setUsername("publicComUser1").setCommunityUserList(asList(new CommunityUser().setCommunity(publicCom))));
    create(new User().setUsername("publicComUser2").setCommunityUserList(asList(new CommunityUser().setCommunity(publicCom))));
  }
  
  @Test
  public void getCommunity_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // public
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}", publicCom.getId())
      .then().statusCode(200)
      .body("communityName", equalTo("publicCom"))
      .body("transactionFee", equalTo(10F))
      .body("description", equalTo("des"))
      .body("status", equalTo("PUBLIC"))
      .body("adminPageUrl", startsWith("url"))
      .body("adminList.adminname", contains(publicComAdmin.getAdminname()));

    // private
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/communities/{id}", privateCom.getId())
    .then().statusCode(200)
    .body("communityName", equalTo("privateCom"))
    .body("status", equalTo("PRIVATE"));
    
    // deleted
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}", deleteCom.getId())
      .then().statusCode(400);
  }
  
  @Test
  public void getCommunity_comAdmin() {
    sessionId = loginAdmin(publicComAdmin.getEmailAddress(), "password");
    
    // my com
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/communities/{id}", publicCom.getId())
    .then().statusCode(200)
    .body("communityName", equalTo("publicCom"));

    // other com
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/communities/{id}", otherCom.getId())
    .then().statusCode(403);
  }
  
  @Test
  public void getCommunity_teller() {
    sessionId = loginAdmin(publicComTeller.getEmailAddress(), "password");
    
    // my com
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/communities/{id}", publicCom.getId())
    .then().statusCode(200)
    .body("communityName", equalTo("publicCom"));

    // other com
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/communities/{id}", otherCom.getId())
    .then().statusCode(403);
  }
}
