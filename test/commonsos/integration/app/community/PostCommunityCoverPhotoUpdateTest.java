package commonsos.integration.app.community;

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
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class PostCommunityCoverPhotoUpdateTest extends IntegrationTest {

  private Community community;
  private User user;
  private User admin;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setStatus(PUBLIC).setName("community"));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    admin =  create(new User().setUsername("admin").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    update(community.setAdminUser(admin));
  }
  
  @Test
  public void updatePhoto_admin() throws URISyntaxException {
    sessionId = login("admin", "pass");
    
    // prepare
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // nocrop
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/communities/{id}/coverPhoto", community.getId())
      .then().statusCode(200);

    // crop
    given()
      .multiPart("photo", photo)
      .multiPart("width", 1000)
      .multiPart("height", 1500)
      .multiPart("x", 100)
      .multiPart("y", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/communities/{id}/coverPhoto", community.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updatePhoto_not_admin() throws URISyntaxException {
    sessionId = login("user", "pass");
    
    // prepare
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // call api
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/communities/{id}/coverPhoto", community.getId())
      .then().statusCode(403);
  }
}
