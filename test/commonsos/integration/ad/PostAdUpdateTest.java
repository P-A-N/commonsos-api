package commonsos.integration.ad;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdType;
import commonsos.repository.community.Community;
import commonsos.repository.user.User;

public class PostAdUpdateTest extends IntegrationTest {

  private Community community;
  private User user;
  private Ad ad;
  private String sessionId;
  
  @Before
  public void setupData() {
    community =  create(new Community().setName("community"));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityId(community.getId()));
    ad =  create(new Ad().setCreatedBy(user.getId()).setCommunityId(community.getId()));
    
    sessionId = login("user", "pass");
  }
  
  @Test
  public void adUpdate() {
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("title", "title");
    requestParam.put("description", "description");
    requestParam.put("points", 10);
    requestParam.put("location", "location");
    requestParam.put("type", "GIVE");

    // call api
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/ads/{id}", ad.getId())
      .then().statusCode(200)
      .body("title", equalTo("title"))
      .body("description", equalTo("description"))
      .body("points", equalTo(10))
      .body("location", equalTo("location"))
      .body("own", equalTo(true))
      .body("payable", equalTo(false))
      .body("type", equalTo("GIVE"))
      .body("createdBy.id", equalTo(user.getId().intValue()))
      .body("createdBy.username", equalTo("user"));
    
    // verify
    emService.get().refresh(ad);
    assertThat(ad.getTitle()).isEqualTo("title");
    assertThat(ad.getDescription()).isEqualTo("description");
    assertThat(ad.getPoints()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(ad.getLocation()).isEqualTo("location");
    assertThat(ad.getType()).isEqualTo(AdType.GIVE);
    assertThat(ad.isDeleted()).isEqualTo(false);
  }
}
