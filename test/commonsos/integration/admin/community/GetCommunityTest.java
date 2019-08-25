package commonsos.integration.admin.community;

import static commonsos.repository.entity.CommunityStatus.PRIVATE;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
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
  private Community publicCom;
  private Community privateCom;
  private Community deleteCom;
  private Admin publicComAdmin1;
  private Admin publicComAdmin2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    publicCom =  create(new Community().setName("publicCom").setStatus(PUBLIC).setFee(BigDecimal.TEN).setDescription("des").setAdminPageUrl("url"));
    privateCom =  create(new Community().setName("privateCom").setStatus(PRIVATE));
    deleteCom =  create(new Community().setName("deleteCom").setStatus(PUBLIC).setDeleted(true));
    
    // create admins
    publicComAdmin1 = create(new Admin().setEmailAddress("publicComAdmin1@before.each.com").setAdminname("publicComAdmin1").setRole(COMMUNITY_ADMIN).setCommunity(publicCom));
    publicComAdmin2 = create(new Admin().setEmailAddress("publicComAdmin2@before.each.com").setAdminname("publicComAdmin2").setRole(TELLER).setCommunity(publicCom));

    // create users
    create(new User().setUsername("publicComUser1").setCommunityUserList(asList(new CommunityUser().setCommunity(publicCom))));
    create(new User().setUsername("publicComUser2").setCommunityUserList(asList(new CommunityUser().setCommunity(publicCom))));
    
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
  }
  
  @Test
  public void getCommunity_public() {
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}", publicCom.getId())
      .then().statusCode(200)
      .body("communityName", equalTo("publicCom"))
      .body("transactionFee", equalTo(10F))
      .body("description", equalTo("des"))
      .body("status", equalTo("PUBLIC"))
      .body("adminPageUrl", startsWith("url"))
      .body("totalMember", equalTo(2))
      .body("adminList.adminname", contains(publicComAdmin1.getAdminname(), publicComAdmin2.getAdminname()));
  }
  
  @Test
  public void getCommunity_private() {
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}", privateCom.getId())
      .then().statusCode(200)
      .body("communityName", equalTo("privateCom"))
      .body("status", equalTo("PRIVATE"))
      .body("totalMember", equalTo(0));
  }
  
  @Test
  public void getCommunity_deleted() {
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}", deleteCom.getId())
      .then().statusCode(400);
  }
}
