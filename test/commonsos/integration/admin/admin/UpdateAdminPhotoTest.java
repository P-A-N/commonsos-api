package commonsos.integration.admin.admin;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class UpdateAdminPhotoTest extends IntegrationTest {

  private Admin ncl1;
  private Admin ncl2;
  private Admin com1Admin;
  private Admin com2Admin;
  private Admin com1Teller;
  private Admin com2Teller;
  private Community com1;
  private Community com2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PRIVATE));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl1 = create(new Admin().setEmailAddress("ncl1@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    ncl2 = create(new Admin().setEmailAddress("ncl2@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com2Admin = create(new Admin().setEmailAddress("com2Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
    com2Teller = create(new Admin().setEmailAddress("com2Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }
  
  @Test
  public void updateAdmin_ncl() throws Exception {
    sessionId = loginAdmin(ncl1.getEmailAddress(), "password");
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);

    // update ncl1 nocrop
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", ncl1.getId())
      .then().statusCode(200);

    // update ncl2 forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", ncl2.getId())
      .then().statusCode(403);

    // update com1Admin crop
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .multiPart("photo[width]", 1000)
      .multiPart("photo[height]", 1500)
      .multiPart("photo[x]", 100)
      .multiPart("photo[y]", 150)
      .when().post("/admin/admins/{id}/photo", com1Admin.getId())
      .then().statusCode(200);

    // update com1Teller nocrop
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", com1Teller.getId())
      .then().statusCode(200);

    // update com1Teller no image
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("hgoe", "hoge")
      .when().post("/admin/admins/{id}/photo", com1Teller.getId())
      .then().statusCode(400);
  }
  
  @Test
  public void updateAdmin_com1Admin() throws Exception {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);

    // update ncl1
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", ncl1.getId())
      .then().statusCode(403);

    // update com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", com1Admin.getId())
      .then().statusCode(200);

    // update com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", com1Teller.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateAdmin_com1Teller() throws Exception {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);

    // update ncl1
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", ncl1.getId())
      .then().statusCode(403);

    // update com1Admin
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", com1Admin.getId())
      .then().statusCode(403);

    // update com1Teller
    given()
      .cookie("JSESSIONID", sessionId)
      .multiPart("photo", photo)
      .when().post("/admin/admins/{id}/photo", com1Teller.getId())
      .then().statusCode(200);
  }
}
