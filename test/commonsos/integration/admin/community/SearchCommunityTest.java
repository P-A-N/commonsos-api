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
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class SearchCommunityTest extends IntegrationTest {

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
  public void searchCommunity() {
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities")
      .then().statusCode(200)
      .body("communityList.communityName", contains("publicCom", "privateCom"))
      .body("communityList.status", contains("PUBLIC", "PRIVATE"))
      .body("communityList.totalMember", contains(2, 0))
      .body("communityList.ethBalance", contains(notNullValue(), notNullValue()))
      .body("communityList.adminList.adminname", contains(
          asList(publicComAdmin1.getAdminname(), publicComAdmin2.getAdminname()),
          asList()));
  }
  
  @Test
  public void searchCommunity_pagination() throws Exception {
    update(publicCom.setDeleted(true));
    update(privateCom.setDeleted(true));
    
    create(new Community().setName("page_com1").setStatus(PUBLIC));
    create(new Community().setName("page_com2").setStatus(PUBLIC));
    create(new Community().setName("page_com3").setStatus(PUBLIC));
    create(new Community().setName("page_com4").setStatus(PUBLIC));
    create(new Community().setName("page_com5").setStatus(PUBLIC));
    create(new Community().setName("page_com6").setStatus(PUBLIC));
    create(new Community().setName("page_com7").setStatus(PUBLIC));
    create(new Community().setName("page_com8").setStatus(PUBLIC));
    create(new Community().setName("page_com9").setStatus(PUBLIC));
    create(new Community().setName("page_com10").setStatus(PUBLIC));
    create(new Community().setName("page_com11").setStatus(PUBLIC));
    create(new Community().setName("page_com12").setStatus(PUBLIC));

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "0", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.communityName", contains(
          "page_com1", "page_com2", "page_com3", "page_com4", "page_com5",
          "page_com6", "page_com7", "page_com8", "page_com9", "page_com10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "1", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.communityName", contains(
          "page_com11", "page_com12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "0", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.communityName", contains(
          "page_com12", "page_com11", "page_com10", "page_com9", "page_com8",
          "page_com7", "page_com6", "page_com5", "page_com4", "page_com3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "1", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.communityName", contains(
          "page_com2", "page_com1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
