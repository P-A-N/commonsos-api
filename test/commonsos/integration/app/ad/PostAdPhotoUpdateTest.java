package commonsos.integration.app.ad;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class PostAdPhotoUpdateTest extends IntegrationTest {

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
  public void adUpdate() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // call api (nocrop)
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/app/v{v}/ads/{id}/photo", APP_API_VERSION.getMajor(), ad.getId())
      .then().statusCode(200);
    
    // call api (crop)
    given()
      .multiPart("photo", photo)
      .multiPart("width", 1000)
      .multiPart("height", 1500)
      .multiPart("x", 100)
      .multiPart("y", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/app/v{v}/ads/{id}/photo", APP_API_VERSION.getMajor(), ad.getId())
      .then().statusCode(200);
  }
}
