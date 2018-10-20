package commonsos.integration;

import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import static io.restassured.RestAssured.given;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.gson.Gson;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import commonsos.EntityManagerService;
import commonsos.service.auth.PasswordService;
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
      "communities");
  protected static PasswordService passwordService = new PasswordService();
  
  @BeforeClass
  public static void startUp() {
    new TestServer().start(new String[]{});
    awaitInitialization();
  }
  
  @BeforeClass
  public static void setupRestAssured() {
    RestAssured.port = TestServer.TEST_SERVER_PORT;
  }
  
  @BeforeClass
  public static void setupDB() {
    emService.init();
  }

  @After
  public void deleteALL() {
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(emService.dataSource()), DELETE_ALL);
    dbSetup.launch();
  }
  
  @AfterClass
  public static void stopServer() {
    stop();
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
    return passwordService.hash(text);
  }
}
