package commonsos.integration.admin.community;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import commonsos.Configuration;
import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import io.restassured.builder.MultiPartSpecBuilder;

public class CreateCommunityTest extends IntegrationTest {

  private Admin ncl;
  private Admin tmp1;
  private Admin tmp2;
  private Admin com1Admin;
  private Community com1;
  private String sessionId;
  
  private Configuration conf = new Configuration();
  private WireMockServer wireMockServer;
  
  @Override
  protected boolean wordpressServerEnable() { return true; }
  
  @BeforeEach
  public void setup() throws Exception {
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    tmp1 = create(new Admin().setEmailAddress("tmp1@before.each.com").setAdminname("tmp1").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN));
    tmp2 = create(new Admin().setEmailAddress("tmp2@before.each.com").setAdminname("tmp2").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN));

    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
  }
  
  @BeforeEach
  public void setupWordPressServer() {
    int port = Integer.parseInt(conf.wordpressServerApiPort());
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port));
    wireMockServer.start();
    WireMock.configureFor("http", "localhost", port);
    stubFor(post(urlEqualTo("/" + conf.wordpressServerApiCreateUserPath()))
        .withHeader("Content-Type", containing("application/json"))
        .withBasicAuth(conf.wordpressBasicAuthorizationUsername(), conf.wordpressBasicAuthorizationPassword())
        .willReturn(ok()));
  }

  @AfterEach
  public void stopWordPressServer() {
    wireMockServer.stop();
  }
  
  @Test
  public void createCommunity() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart(new MultiPartSpecBuilder("コミュニティ１").controlName("communityName").charset("UTF-8").build())
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "1.5")
      .multiPart("description", "description")
      .multiPart("adminIdList", String.format("%d,%d", tmp1.getId(), tmp2.getId()))
      .multiPart("description", "description")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200)
      .body("transactionFee", equalTo(1.5F))
      .body("status", equalTo("PRIVATE"))
      .body("adminPageUrl", startsWith("https://admin.test.commonsos.love/"))
      .body("createdAt", notNullValue())
      .body("adminList.id", contains(tmp1.getId().intValue(), tmp2.getId().intValue()))
      .body("adminList.adminname", contains("tmp1", "tmp2"));

    // verify db
    Community c = emService.get().createQuery("FROM Community WHERE name = 'コミュニティ１'", Community.class).getSingleResult();
    assertThat(c.getWordpressAccountId()).isEqualTo("community1_wp");
    assertThat(c.getWordpressAccountEmailAddress()).isEqualTo("community1@test.com");
    
    Thread.sleep(1000);
    WireMock.verify(1, postRequestedFor(urlEqualTo("/" + conf.wordpressServerApiCreateUserPath())).withRequestBody(
        WireMock.equalTo(String.format("{\"username\":\"community1_wp\",\"email\":\"community1@test.com\",\"name\":\"コミュニティ１\",\"password\":\"test\",\"roles\":\"nothing\",\"community_id\":%d}", c.getId()))));
  }
  
  @Test
  public void createCommunity_notNcl() {
    sessionId = loginAdmin(tmp1.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "1.5")
      .multiPart("description", "description")
      .multiPart("adminIdList", String.format("%d,%d", tmp1.getId(), tmp2.getId()))
      .multiPart("description", "description")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(403);
  }
  
  @Test
  public void createCommunity_otherComAdmin() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "1.5")
      .multiPart("description", "description")
      .multiPart("adminIdList", String.format("%d", com1Admin.getId()))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(468);
  }
  
  @Test
  public void createCommunity_notExistsAdmin() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "1.5")
      .multiPart("description", "description")
      .multiPart("adminIdList", String.format("%d", -1))
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(400);
  }
  
  @Test
  public void createCommunity_noAdmin() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "1.5")
      .multiPart("description", "description")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200);
  }
  
  @Test
  public void createCommunity_tooMuchFee() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "101")
      .multiPart("description", "description")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(468);
  }
  
  @Test
  public void createCommunity_tooLessFee() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "-1")
      .multiPart("description", "description")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(468);
  }
  
  @Test
  public void createCommunity_tooDetailFee() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("transactionFee", "1.9999999")
      .multiPart("description", "description")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200)
      .body("transactionFee", equalTo(1.99F));
  }
  
  @Test
  public void createCommunity_noFee() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("description", "description")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200)
      .body("transactionFee", equalTo(0F));
  }
  
  @Test
  public void createCommunity_photo_noCrop() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("description", "description")
      .multiPart("photo", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200);
  }
  
  @Test
  public void createCommunity_photo_crop() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("description", "description")
      .multiPart("photo", photo)
      .multiPart("photo[width]", 1000)
      .multiPart("photo[height]", 1500)
      .multiPart("photo[x]", 100)
      .multiPart("photo[y]", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200);
  }
  
  @Test
  public void createCommunity_coverPhoto_noCrop() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("description", "description")
      .multiPart("coverPhoto", photo)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200);
  }
  
  @Test
  public void createCommunity_coverPhoto_crop() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File photo = new File(uri);

    // create temporary account
    given()
      .multiPart("communityName", "community1")
      .multiPart("tokenName", "com1")
      .multiPart("tokenSymbol", "com")
      .multiPart("wordpressAccountId", "community1_wp")
      .multiPart("wordpressAccountEmailAddress", "community1@test.com")
      .multiPart("description", "description")
      .multiPart("coverPhoto", photo)
      .multiPart("coverPhoto[width]", 1000)
      .multiPart("coverPhoto[height]", 1500)
      .multiPart("coverPhoto[x]", 100)
      .multiPart("coverPhoto[y]", 150)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200);
  }
}
