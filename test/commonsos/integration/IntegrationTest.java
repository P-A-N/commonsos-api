package commonsos.integration;

import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import static io.restassured.RestAssured.given;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import com.google.gson.Gson;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import commonsos.repository.EntityManagerService;
import commonsos.service.crypto.CryptoService;
import io.restassured.RestAssured;

public class IntegrationTest {
  
  protected static Gson gson = new Gson();
  protected static EntityManagerService emService = new TestEntityManagerService();
  protected static Operation DELETE_ALL = deleteAllFrom(
      "message_thread_parties",
      "users",
      "ads",
      "messages",
      "message_threads",
      "transactions",
      "communities",
      "temporary_community_users",
      "temporary_users",
      "temporary_email_address",
      "password_reset_request");
  protected static Wiser wiser;
  protected static CryptoService cryptoService = new CryptoService();
  
  @BeforeClass
  public static void setupIntegrationTest() {
    // Test Server
    new TestServer().start(new String[]{});
    awaitInitialization();

    // RestAssured
    RestAssured.port = TestServer.TEST_SERVER_PORT;

    // DB
    emService.init();
    
    // SMTP Server
    wiser = new Wiser();
    wiser.setPort(TestEmailService.TEST_SMTP_SERVER_PORT);
    wiser.start();
  }

  @After
  public void cleanupTestData() {
    // DB
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(emService.dataSource()), DELETE_ALL);
    dbSetup.launch();
    
    // SMTP Server
    wiser.getMessages().clear();
  }
  
  @AfterClass
  public static void stopIntegrationTest() {
    // TestServer
    stop();
    
    // SMTP Server
    wiser.stop();
  }
  
  public static String login(String username, String password) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", username);
    requestParam.put("password", password);
    
    String sessionId = given()
      .body(gson.toJson(requestParam))
      .when().post("/login")
      .then().statusCode(200)
      .extract().cookie("JSESSIONID");
    
    return sessionId;
  }
  
  public static <T> T create(T entity) {
    emService.runInTransaction(() -> emService.get().persist(entity));
    return entity;
  }
  
  public static <T> T update(T entity) {
    emService.runInTransaction(() -> emService.get().merge(entity));
    return entity;
  }

  public static String hash(String text) {
    return cryptoService.encryptoPassword(text);
  }
  
  public static String extractAccessId(WiserMessage wiseMessage) throws Exception {
    String content = wiseMessage.getMimeMessage().getContent().toString();
    Pattern p = Pattern.compile("^https://.*$", Pattern.MULTILINE);
    Matcher m = p.matcher(content);
    if (m.find()) {
      String url = m.group();
      String accessId = url.substring(url.lastIndexOf('/') + 1);
      return accessId;
    }
    
    return "";
  }
}
