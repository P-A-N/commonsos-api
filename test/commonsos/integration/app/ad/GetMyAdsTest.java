package commonsos.integration.app.ad;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetMyAdsTest extends IntegrationTest {

  private Community community;
  private User user1;
  private User user2;
  private Ad ad1_1;
  private Ad ad1_2;
  private Ad ad2;
  private String sessionId;
  
  @BeforeEach
  public void setupData() throws Exception {
    community = create(new Community().setName("community").setStatus(PUBLIC));
    user1 = create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 = create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad1_1 = create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community.getId()).setPoints(TEN));
    ad1_2 = create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community.getId()).setPoints(TEN));
    ad2 = create(new Ad().setCreatedBy(user2.getId()).setCommunityId(community.getId()).setPoints(TEN));
  }
  
  @Test
  public void myAds() {
    // search ads for user1
    sessionId = loginApp("user1", "pass");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/my-ads")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad1_1.getId().intValue(),
          ad1_2.getId().intValue()));

    // search ads for user2
    sessionId = loginApp("user2", "pass");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/my-ads")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad2.getId().intValue()));
  }

  
  @Test
  public void myAds_pagination() throws Exception {
    // prepare
    Community pageCommunity1 =  create(new Community().setName("page_community1").setStatus(PUBLIC));
    Community pageCommunity2 =  create(new Community().setName("page_community2").setStatus(PUBLIC));
    User pageUser = create(new User().setUsername("page_user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity1), new CommunityUser().setCommunity(pageCommunity2))));
    create(new Ad().setTitle("page_ad1").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity1.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad2").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity2.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad3").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity1.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad4").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity2.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad5").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity1.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad6").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity2.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad7").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity1.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad8").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity2.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad9").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity1.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad10").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity2.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad11").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity1.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad12").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity2.getId()).setPoints(TEN));

    sessionId = loginApp("page_user1", "pass");
    
    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/my-ads?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "0", "10", "ASC")
      .then().statusCode(200)
      .body("adList.title", contains(
          "page_ad1", "page_ad2", "page_ad3", "page_ad4", "page_ad5",
          "page_ad6", "page_ad7", "page_ad8", "page_ad9", "page_ad10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));
    
    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/my-ads?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "1", "10", "ASC")
      .then().statusCode(200)
      .body("adList.title", contains(
          "page_ad11", "page_ad12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));
    
    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/my-ads?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "0", "10", "DESC")
      .then().statusCode(200)
      .body("adList.title", contains(
          "page_ad12", "page_ad11", "page_ad10", "page_ad9", "page_ad8",
          "page_ad7", "page_ad6", "page_ad5", "page_ad4", "page_ad3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
    
    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/my-ads?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "1", "10", "DESC")
      .then().statusCode(200)
      .body("adList.title", contains(
          "page_ad2", "page_ad1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
