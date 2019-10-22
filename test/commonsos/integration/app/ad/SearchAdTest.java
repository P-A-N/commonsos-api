package commonsos.integration.app.ad;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
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

public class SearchAdTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User fooUser;
  private User barUser;
  private User otherCommunityUser;
  private Ad ad;
  private Ad fooAd;
  private Ad fooAd2;
  private String sessionId;
  
  @BeforeEach
  public void setupData() throws Exception {
    community = create(new Community().setName("community").setPublishStatus(PUBLIC));
    otherCommunity = create(new Community().setName("otherCommunity").setPublishStatus(PUBLIC));
    /*user = */create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    fooUser = create(new User().setUsername("fooUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    barUser = create(new User().setUsername("barUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherCommunityUser = create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));
    ad = create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(fooUser.getId()).setCommunityId(community.getId()).setPoints(TEN));
    /*ad2 = */create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN));
    fooAd = create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setTitle("foo"));
    fooAd2 = create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setDescription("foo"));
    /*fooAd3 = */create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(otherCommunityUser.getId()).setCommunityId(otherCommunityUser.getId()).setPoints(TEN).setTitle("foo"));
    /*barAd =  */create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setTitle("bar"));
    /*privateAd = */create(new Ad().setPublishStatus(PRIVATE).setCreatedUserId(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setDescription("foo"));
    
    sessionId = loginApp("user", "pass");
  }
  
  @Test
  public void searchAd() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/ads?communityId={communityId}&filter={filter}", APP_API_VERSION.getMajor(), community.getId(), "foo")
      .then().statusCode(200)
      .body("adList.id", contains(
          ad.getId().intValue(),
          fooAd.getId().intValue(),
          fooAd2.getId().intValue()))
      .body("adList.communityId", contains(
          ad.getCommunityId().intValue(),
          fooAd.getCommunityId().intValue(),
          fooAd2.getCommunityId().intValue()));
  }
  
  @Test
  public void searchAd_pagination() throws Exception {
    // prepare
    Community pageCommunity =  create(new Community().setName("page_community").setPublishStatus(PUBLIC));
    User pageUser = create(new User().setUsername("page_user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad1").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad2").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad3").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad4").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad5").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad6").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad7").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad8").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad9").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad10").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad11").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setPublishStatus(PUBLIC).setTitle("page_ad12").setCreatedUserId(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));

    sessionId = loginApp("user", "pass");
    
    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "0", "10", "ASC")
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
      .when().get("/app/v{v}/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "1", "10", "ASC")
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
      .when().get("/app/v{v}/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "0", "10", "DESC")
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
      .when().get("/app/v{v}/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "1", "10", "DESC")
      .then().statusCode(200)
      .body("adList.title", contains(
          "page_ad2", "page_ad1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
