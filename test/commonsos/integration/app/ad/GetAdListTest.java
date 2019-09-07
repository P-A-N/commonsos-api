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

public class GetAdListTest extends IntegrationTest {

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
    community = create(new Community().setName("community").setStatus(PUBLIC));
    otherCommunity = create(new Community().setName("otherCommunity").setStatus(PUBLIC));
    /*user = */create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    fooUser = create(new User().setUsername("fooUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    barUser = create(new User().setUsername("barUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherCommunityUser = create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));
    ad = create(new Ad().setCreatedBy(fooUser.getId()).setCommunityId(community.getId()).setPoints(TEN));
    /*ad2 = */create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN));
    fooAd = create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setTitle("foo"));
    fooAd2 = create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setDescription("foo"));
    /*fooAd3 = */create(new Ad().setCreatedBy(otherCommunityUser.getId()).setCommunityId(otherCommunityUser.getId()).setPoints(TEN).setTitle("foo"));
    /*barAd =  */create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setTitle("bar"));
    
    sessionId = loginApp("user", "pass");
  }
  
  @Test
  public void adList() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/ads?communityId={communityId}&filter={filter}", community.getId(), "foo")
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
  public void adList_pagination() throws Exception {
    // prepare
    Community pageCommunity =  create(new Community().setName("page_community").setStatus(PUBLIC));
    User pageUser = create(new User().setUsername("page_user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new Ad().setTitle("page_ad1").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad2").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad3").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad4").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad5").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad6").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad7").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad8").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad9").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad10").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad11").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));
    create(new Ad().setTitle("page_ad12").setCreatedBy(pageUser.getId()).setCommunityId(pageCommunity.getId()).setPoints(TEN));

    sessionId = loginApp("user", "pass");
    
    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v99/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageCommunity.getId(), "page", "0", "10", "ASC")
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
      .when().get("/app/v99/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageCommunity.getId(), "page", "1", "10", "ASC")
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
      .when().get("/app/v99/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageCommunity.getId(), "page", "0", "10", "DESC")
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
      .when().get("/app/v99/ads?communityId={communityId}&filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageCommunity.getId(), "page", "1", "10", "DESC")
      .then().statusCode(200)
      .body("adList.title", contains(
          "page_ad2", "page_ad1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
