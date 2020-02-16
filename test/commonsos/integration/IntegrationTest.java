package commonsos.integration;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.integration.TestEntityManagerService.DELETE_ALL;
import static io.restassured.RestAssured.given;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import com.google.gson.Gson;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;

import commonsos.service.blockchain.BlockchainService;
import commonsos.service.crypto.CryptoService;
import io.restassured.RestAssured;

@TestInstance(Lifecycle.PER_CLASS)
public class IntegrationTest {
  
  protected Gson gson = new Gson();
  protected TestEntityManagerService emService = new TestEntityManagerService();
  protected Wiser wiser = new Wiser();
  protected CryptoService cryptoService = new CryptoService();
  protected BlockchainService blockchainService;

  protected boolean isBlockchainEnable() { return false; }
  protected boolean imageuploadEnable() { return false; }
  protected boolean wordpressServerEnable() { return false; }
  
  @BeforeAll
  public void setupIntegrationTest() {
    // DB
    emService.init();
    emService.clearDbAndMigrate();

    // Test Server
    TestServer testServer = new TestServer();
    testServer.setBlockchainEnable(isBlockchainEnable());
    testServer.setImageuploadEnable(imageuploadEnable());
    testServer.setWordpressServerEnable(wordpressServerEnable());
    testServer.start(new String[]{});
    blockchainService = testServer.getBlockchainService();
    awaitInitialization();
    
    // RestAssured
    RestAssured.port = TestServer.TEST_SERVER_PORT;

    // SMTP Server
    wiser.setPort(TestEmailService.TEST_SMTP_SERVER_PORT);
    wiser.start();
  }

  @AfterEach
  public void cleanupTestData() {
    // DB
    emService.close();
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(emService.dataSource()), DELETE_ALL);
    dbSetup.launch();
    
    // SMTP Server
    wiser.getMessages().clear();
  }
  
  @AfterAll
  public void stopIntegrationTest() {
    // DB
    emService.closeFactory();
    
    // TestServer
    stop();
    
    // SMTP Server
    wiser.stop();
  }
  
  public String loginApp(String username, String password) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", username);
    requestParam.put("password", password);
    
    String sessionId = given()
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .extract().cookie("JSESSIONID");
    
    return sessionId;
  }
  
  public void failLoginApp(String username, String password) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", username);
    requestParam.put("password", password);
    
    given()
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(401);
  }

  public String loginAdmin(String emailAddress, String password) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("emailAddress", emailAddress);
    requestParam.put("password", password);
    
    String sessionId = given()
      .body(gson.toJson(requestParam))
      .when().post("/admin/login")
      .then().statusCode(200)
      .extract().cookie("JSESSIONID");
    
    return sessionId;
  }

  public void failLoginAdmin(String emailAddress, String password) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("emailAddress", emailAddress);
    requestParam.put("password", password);
    
    given()
      .body(gson.toJson(requestParam))
      .when().post("/admin/login")
      .then().statusCode(401);
  }

  public <T> T create(T entity) throws Exception {
    emService.runInTransaction(() -> emService.get().persist(entity));
    emService.close();
    Thread.sleep(1);
    return entity;
  }
  
  public <T> T update(T entity) throws Exception {
    emService.runInTransaction(() -> emService.get().merge(entity));
    emService.close();
    Thread.sleep(1);
    return entity;
  }

  public String hash(String text) {
    return cryptoService.encryptoPassword(text);
  }
  
  public String extractAccessId(WiserMessage wiseMessage) throws Exception {
    return extractAccessId(wiseMessage, "/");
  }
  
  public String extractAccessId(WiserMessage wiseMessage, String lastIndex) throws Exception {
    String content = wiseMessage.getMimeMessage().getContent().toString();
    Pattern p = Pattern.compile("^https://.*$", Pattern.MULTILINE);
    Matcher m = p.matcher(content);
    if (m.find()) {
      String url = m.group();
      String accessId = url.substring(url.lastIndexOf(lastIndex) + 1);
      return accessId;
    }
    
    return "";
  }
}
