package commonsos.integration.admin.community;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class UpdateCommunityPhotoTest extends IntegrationTest {

  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Community com1;
  private Community com2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PRIVATE));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }
  
  @Test
  public void updateCommunityPhoto_ncl() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // update community photo [no_crop]
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/photo", com1.getId())
      .then().statusCode(200)
      .body("communityName", equalTo("com1"));
    
    // update community photo [crop]
    given()
      .multiPart("photo", photo)
      .multiPart("photo[width]", 1000)
      .multiPart("photo[height]", 1500)
      .multiPart("photo[x]", 100)
      .multiPart("photo[y]", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/photo", com2.getId())
      .then().statusCode(200)
      .body("communityName", equalTo("com2"));
  }
  
  @Test
  public void updateCommunityPhoto_com1Admin() throws Exception {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // update community photo [no_crop]
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/photo", com1.getId())
      .then().statusCode(200);
    
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/photo", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void updateCommunityPhoto_com1Teller() throws Exception {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // update community photo [no_crop]
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/photo", com1.getId())
      .then().statusCode(403);
    
    given()
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/photo", com2.getId())
      .then().statusCode(403);
  }
}
