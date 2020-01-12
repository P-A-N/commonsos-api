package commonsos.integration.admin.admin;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import io.restassured.builder.MultiPartSpecBuilder;

public class CreateAdminTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    sessionId = loginAdmin("ncl@before.each.com", "password");
  }
  
  @Test
  public void createAdmin_communityAdimn_byNcl() throws Exception {
    // create temporary account
    given()
      .multiPart(new MultiPartSpecBuilder("テスト　管理者").controlName("adminname").charset("UTF-8").build())
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId().toString())
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .multiPart("emailAddress", "comAdmin@test.com")
      .multiPart("telNo", "00000000000")
      .multiPart(new MultiPartSpecBuilder("テスト部").controlName("department").charset("UTF-8").build())
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("comAdmin@test.com");
    String accessId = extractAccessId(messages.get(0), "#");

    // login should fail at now
    failLoginAdmin("comAdmin@test.com", "password");

    // complete create account
    given()
      .when().post("/admin/create-admin/{accessId}", accessId)
      .then().statusCode(200)
      .body("adminname", equalTo("テスト　管理者"))
      .body("communityId", equalTo(com1.getId().intValue()))
      .body("roleId", equalTo(COMMUNITY_ADMIN.getId().intValue()))
      .body("rolename", equalTo("コミュニティ管理者"))
      .body("emailAddress", equalTo("comAdmin@test.com"))
      .body("telNo", equalTo("00000000000"))
      .body("department", equalTo("テスト部"))
      .body("loggedinAt", notNullValue())
      .body("createdAt", notNullValue());

    // login should success at now
    loginAdmin("comAdmin@test.com", "password");
    
    // failed [email address is taken]
    given()
      .multiPart("emailAddress", "comAdmin@test.com")
      .multiPart(new MultiPartSpecBuilder("テスト").controlName("adminname").charset("UTF-8").build())
      .multiPart("password", "password")
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(468)
      .body("key", equalTo("error.emailAddressTaken"));

    // failed [adminname is taken]
    given()
      .multiPart("emailAddress", "test@test.com")
      .multiPart(new MultiPartSpecBuilder("テスト　管理者").controlName("adminname").charset("UTF-8").build())
      .multiPart("password", "password")
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(468)
      .body("key", equalTo("error.adminnameTaken"));
  }
  
  @Test
  public void createAdmin_noComAdimn_byNcl() throws Exception {
    // create temporary account
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .multiPart("emailAddress", "comAdmin@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("comAdmin@test.com");
    String accessId = extractAccessId(messages.get(0), "#");

    // login should fail at now
    failLoginAdmin("comAdmin@test.com", "password");

    // complete create account
    given()
      .when().post("/admin/create-admin/{accessId}", accessId)
      .then().statusCode(200);

    // login should success at now
    loginAdmin("comAdmin@test.com", "password");
  }
  
  @Test
  public void createAdmin_teller_byNcl() throws Exception {
    // create temporary account
    given()
      .multiPart("adminname", "teller")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId().toString())
      .multiPart("roleId", TELLER.getId().toString())
      .multiPart("emailAddress", "teller@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("teller@test.com");
    String accessId = extractAccessId(messages.get(0), "#");

    // login should fail at now
    failLoginAdmin("teller@test.com", "password");

    // complete create account
    given()
      .when().post("/admin/create-admin/{accessId}", accessId)
      .then().statusCode(200);

    // login should success at now
    loginAdmin("teller@test.com", "password");
  }
  
  @Test
  public void createAdmin_noComTeller_byNcl() throws Exception {
    // create temporary account
    given()
      .multiPart("adminname", "teller")
      .multiPart("password", "password")
      .multiPart("roleId", TELLER.getId().toString())
      .multiPart("emailAddress", "teller@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("teller@test.com");
    String accessId = extractAccessId(messages.get(0), "#");

    // login should fail at now
    failLoginAdmin("teller@test.com", "password");

    // complete create account
    given()
      .when().post("/admin/create-admin/{accessId}", accessId)
      .then().statusCode(200);

    // login should success at now
    loginAdmin("teller@test.com", "password");
  }
  
  @Test
  public void createAdmin_ncl_byNcl() throws Exception {
    // create temporary account
    given()
      .multiPart("adminname", "ncl")
      .multiPart("password", "password")
      .multiPart("roleId", NCL.getId().toString())
      .multiPart("emailAddress", "ncl@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("ncl@test.com");
    String accessId = extractAccessId(messages.get(0), "#");

    // login should fail at now
    failLoginAdmin("ncl@test.com", "password");

    // complete create account
    given()
      .when().post("/admin/create-admin/{accessId}", accessId)
      .then().statusCode(200);

    // login should success at now
    loginAdmin("ncl@test.com", "password");
  }
  
  @Test
  public void createAdmin_comAdmin_byComAdmin() throws Exception {
    sessionId = loginAdmin("com1Admin@before.each.com", "password");
    
    // can't create non community admin
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("roleId", COMMUNITY_ADMIN.getId())
      .multiPart("emailAddress", "comAdmin@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
    
    // can't create other community admin
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("communityId", com2.getId())
      .multiPart("roleId", COMMUNITY_ADMIN.getId())
      .multiPart("emailAddress", "comAdmin@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
    
    // can create own community admin
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId())
      .multiPart("roleId", COMMUNITY_ADMIN.getId())
      .multiPart("emailAddress", "comAdmin@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // complete create account
    String accessId = extractAccessId(wiser.getMessages().get(0), "#");
    given()
      .when().post("/admin/create-admin/{accessId}", accessId)
      .then().statusCode(200);

    loginAdmin("comAdmin@test.com", "password");
  }
  
  @Test
  public void createAdmin_teller_byComAdmin() throws Exception {
    sessionId = loginAdmin("com1Admin@before.each.com", "password");
    
    // can't create non community teller
    given()
      .multiPart("adminname", "teller")
      .multiPart("password", "password")
      .multiPart("roleId", TELLER.getId())
      .multiPart("emailAddress", "teller@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
    
    // can't create other community teller
    given()
      .multiPart("adminname", "teller")
      .multiPart("password", "password")
      .multiPart("communityId", com2.getId())
      .multiPart("roleId", TELLER.getId())
      .multiPart("emailAddress", "teller@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
    
    // can create own community teller
    given()
      .multiPart("adminname", "teller")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId())
      .multiPart("roleId", TELLER.getId())
      .multiPart("emailAddress", "teller@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // complete create account
    String accessId = extractAccessId(wiser.getMessages().get(0), "#");
    given()
      .when().post("/admin/create-admin/{accessId}", accessId)
      .then().statusCode(200);

    loginAdmin("teller@test.com", "password");
  }
  
  @Test
  public void createAdmin_ncl_byComAdmin() throws Exception {
    sessionId = loginAdmin("com1Admin@before.each.com", "password");
    
    given()
      .multiPart("adminname", "teller")
      .multiPart("password", "password")
      .multiPart("roleId", NCL.getId())
      .multiPart("emailAddress", "ncl@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
  }
  
  @Test
  public void createAdmin_byTeller() throws Exception {
    sessionId = loginAdmin("com1Teller@before.each.com", "password");
    
    given()
      .multiPart("adminname", "ncl")
      .multiPart("password", "password")
      .multiPart("roleId", NCL.getId())
      .multiPart("emailAddress", "ncl@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
    
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId())
      .multiPart("roleId", COMMUNITY_ADMIN.getId())
      .multiPart("emailAddress", "comAdmin@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
    
    given()
      .multiPart("adminname", "teller")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId())
      .multiPart("roleId", TELLER.getId())
      .multiPart("emailAddress", "teller@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(403);
  }
  
  @Test
  public void createAdmin_emailAddressIsTaken() throws Exception {
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId().toString())
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .multiPart("emailAddress", "ncl@before.each.com") // email address taken by ncl
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(468);
    
    // create temporary account
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId().toString())
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .multiPart("emailAddress", "comAdmin@test.com")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    given()
      .multiPart("adminname", "adminname")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId().toString())
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .multiPart("emailAddress", "comAdmin@test.com") // email address taken by self
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(468);
  }
  
  @Test
  public void createAdmin_photoUpload() throws Exception {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);
    
    // no crop
    given()
      .multiPart("adminname", "comAdmin")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId().toString())
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .multiPart("emailAddress", "comAdmin@test.com")
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);

    // crop
    given()
      .multiPart("adminname", "comAdmin2")
      .multiPart("password", "password")
      .multiPart("communityId", com1.getId().toString())
      .multiPart("roleId", COMMUNITY_ADMIN.getId().toString())
      .multiPart("emailAddress", "comAdmin2@test.com")
      .multiPart("photo", photo)
      .multiPart("photo[width]", 1000)
      .multiPart("photo[height]", 1500)
      .multiPart("photo[x]", 100)
      .multiPart("photo[y]", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/create-admin")
      .then().statusCode(200);
  }
}
