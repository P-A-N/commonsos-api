package commonsos.integration.admin.ad;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;

public class DeleteAdTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User com1com2User;
  private Ad ad1;
  private Ad ad2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com1com2User = create(new User().setUsername("com1com2User").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    
    ad1 = create(new Ad().setCommunityId(com1.getId()).setCreatedUserId(com1com2User.getId()).setPublishStatus(PUBLIC).setType(AdType.GIVE));
    ad2 = create(new Ad().setCommunityId(com2.getId()).setCreatedUserId(com1com2User.getId()).setPublishStatus(PRIVATE).setType(AdType.GIVE));
    create(new TokenTransaction().setAdId(ad1.getId()).setCommunityId(com1.getId()));
  }
  
  @Test
  public void deleteAd_byNcl() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // delete ad(has paid)
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad1.getId())
      .then().statusCode(200);
    
    // verify deleted
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/ads/{id}", ad1.getId())
      .then().statusCode(400);

    // verify db
    Ad adDeleted = emService.get().find(Ad.class, ad1.getId());
    assertThat(adDeleted.isDeleted()).isTrue();

    
    // update private ad
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad2.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void deleteAd_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // delete ad
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad1.getId())
      .then().statusCode(200);

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void deleteAd_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // delete ad
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad1.getId())
      .then().statusCode(200);

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad2.getId())
      .then().statusCode(403);
  }
}
