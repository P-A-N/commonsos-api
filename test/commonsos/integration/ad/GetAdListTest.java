package commonsos.integration.ad;

import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class GetAdListTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user;
  private User fooUser;
  private User barUser;
  private User otherCommunityUser;
  private Ad ad;
  private Ad ad2;
  private Ad fooAd;
  private Ad fooAd2;
  private Ad fooAd3;
  private Ad barAd;
  private String sessionId;
  
  @Before
  public void setupData() {
    community =  create(new Community().setName("community"));
    otherCommunity =  create(new Community().setName("otherCommunity"));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    fooUser =  create(new User().setUsername("fooUser").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    barUser =  create(new User().setUsername("barUser").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityList(asList(otherCommunity)));
    ad =  create(new Ad().setCreatedBy(fooUser.getId()).setCommunityId(community.getId()).setPoints(TEN));
    ad2 =  create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN));
    fooAd =  create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setTitle("foo"));
    fooAd2 =  create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setDescription("foo"));
    fooAd3 =  create(new Ad().setCreatedBy(otherCommunityUser.getId()).setCommunityId(otherCommunityUser.getId()).setPoints(TEN).setTitle("foo"));
    barAd =  create(new Ad().setCreatedBy(barUser.getId()).setCommunityId(community.getId()).setPoints(TEN).setTitle("bar"));
    
    sessionId = login("user", "pass");
  }
  
  @Test
  public void adList() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/ads?communityId={communityId}&filter={filter}", community.getId(), "foo")
      .then().statusCode(200)
      .body("id", contains(
          ad.getId().intValue(),
          fooAd.getId().intValue(),
          fooAd2.getId().intValue()))
      .body("communityId", contains(
          ad.getCommunityId().intValue(),
          fooAd.getCommunityId().intValue(),
          fooAd2.getCommunityId().intValue()));
  }
}
