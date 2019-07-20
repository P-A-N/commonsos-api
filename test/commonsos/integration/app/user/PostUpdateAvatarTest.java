package commonsos.integration.app.user;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.User;

public class PostUpdateAvatarTest extends IntegrationTest {

  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() {
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com"));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void updateUsername_jpeg() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // nocrop
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(200);
    
    // crop
    given()
      .multiPart("photo", photo)
      .multiPart("width", 1000)
      .multiPart("height", 1500)
      .multiPart("x", 100)
      .multiPart("y", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateUsername_png() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.png");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // nocrop
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(200);
    
    // crop
    given()
      .multiPart("photo", photo)
      .multiPart("width", 1000)
      .multiPart("height", 1500)
      .multiPart("x", 100)
      .multiPart("y", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateUsername_svg() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.svg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // nocrop
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(468);
    
    // crop
    given()
      .multiPart("photo", photo)
      .multiPart("width", 1000)
      .multiPart("height", 1500)
      .multiPart("x", 100)
      .multiPart("y", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(468);
  }
  
  @Test
  public void updateUsername_txt() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.txt");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // nocrop
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(468);
    
    // crop
    given()
      .multiPart("photo", photo)
      .multiPart("width", 1000)
      .multiPart("height", 1500)
      .multiPart("x", 100)
      .multiPart("y", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{id}/avatar", user.getId())
      .then().statusCode(468);
  }
}
