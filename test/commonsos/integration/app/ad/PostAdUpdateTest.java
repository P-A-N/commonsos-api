package commonsos.integration.app.ad;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class PostAdUpdateTest extends IntegrationTest {

  private Community community;
  private User user;
  private Ad ad;
  private String sessionId;
  
  @BeforeEach
  public void setupData() throws Exception {
    community =  create(new Community().setName("community").setStatus(PUBLIC));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    ad =  create(new Ad().setCreatedBy(user.getId()).setCommunityId(community.getId()));
    
    sessionId = loginApp("user", "pass");
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
      .when().post("/app/v99/ads/{id}", ad.getId())
      .then().statusCode(200)
      .body("title", equalTo("title"))
      .body("description", equalTo("description"))
      .body("points", equalTo(10))
      .body("location", equalTo("location"))
      .body("own", equalTo(true))
      .body("type", equalTo("GIVE"))
      .body("createdBy.id", equalTo(user.getId().intValue()))
      .body("createdBy.username", equalTo("user"));
    
    // verify
    Ad actual = emService.get().find(Ad.class, ad.getId());
    assertThat(actual.getTitle()).isEqualTo("title");
    assertThat(actual.getDescription()).isEqualTo("description");
    assertThat(actual.getPoints()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(actual.getLocation()).isEqualTo("location");
    assertThat(actual.getType()).isEqualTo(AdType.GIVE);
    assertThat(actual.isDeleted()).isEqualTo(false);
  }
}
