package commonsos.integration.admin.ad;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class SearchAdTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User com1com2User;
  private Ad com1ad1;
  private Ad com1ad2;
  private Ad com1ad3deleted;
  private Ad com2ad1;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com1com2User = create(new User().setUsername("com1com2User").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    
    com1ad1 = create(new Ad().setCommunityId(com1.getId()).setCreatedUserId(com1com2User.getId()).setPublishStatus(PUBLIC));
    com1ad2 = create(new Ad().setCommunityId(com1.getId()).setCreatedUserId(com1com2User.getId()).setPublishStatus(PRIVATE));
    com1ad3deleted = create(new Ad().setCommunityId(com1.getId()).setCreatedUserId(com1com2User.getId()).setPublishStatus(PUBLIC).setDeleted(true));
    com2ad1 = create(new Ad().setCommunityId(com2.getId()).setCreatedUserId(com1com2User.getId()).setPublishStatus(PUBLIC));
  }
  
  @Test
  public void searchAd_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // search ad
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("adList.id", contains(com1ad1.getId().intValue(), com1ad2.getId().intValue()))
      .body("adList.community.id", contains(com1.getId().intValue(), com1.getId().intValue()))
      .body("adList.community.name", contains("com1", "com1"))
      .body("adList.createdBy.id", contains(com1com2User.getId().intValue(), com1com2User.getId().intValue()))
      .body("adList.createdBy.username", contains(com1com2User.getUsername(), com1com2User.getUsername()))
      .body("adList.publishStatus", contains("PUBLIC", "PRIVATE"));

    // search ad
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}", com2.getId())
      .then().statusCode(200)
      .body("adList.id", contains(com2ad1.getId().intValue()));
  }
  
  @Test
  public void searchAd_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // search ad
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("adList.id", contains(com1ad1.getId().intValue(), com1ad2.getId().intValue()));

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void searchAd_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // search ad
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("adList.id", contains(com1ad1.getId().intValue(), com1ad2.getId().intValue()));

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void searchAd_pagination() throws Exception {
    Community com =  create(new Community().setName("com").setPublishStatus(PUBLIC));
    User user = create(new User().setUsername("user").setCommunityUserList(asList(new CommunityUser().setCommunity(com))));
    Ad ad1 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad2 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad3 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad4 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad5 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad6 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad7 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad8 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad9 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad10 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad11 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    Ad ad12 = create(new Ad().setCommunityId(com.getId()).setCreatedUserId(user.getId()).setPublishStatus(PUBLIC));
    
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad1.getId().intValue(), ad2.getId().intValue(), ad3.getId().intValue(), ad4.getId().intValue(), ad5.getId().intValue(),
          ad6.getId().intValue(), ad7.getId().intValue(), ad8.getId().intValue(), ad9.getId().intValue(), ad10.getId().intValue()))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad11.getId().intValue(), ad12.getId().intValue()))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad12.getId().intValue(), ad11.getId().intValue(), ad10.getId().intValue(), ad9.getId().intValue(), ad8.getId().intValue(),
          ad7.getId().intValue(), ad6.getId().intValue(), ad5.getId().intValue(), ad4.getId().intValue(), ad3.getId().intValue()))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads?communityId={id}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad2.getId().intValue(), ad1.getId().intValue()))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
