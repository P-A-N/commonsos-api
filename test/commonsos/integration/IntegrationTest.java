package commonsos.integration;

import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.gson.Gson;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import commonsos.EntityManagerService;
import io.restassured.RestAssured;

public class IntegrationTest {
  
  protected static Gson gson = new Gson();
  protected static EntityManagerService emService = new TestEntityManagerService();
  protected static Operation DELETE_ALL = deleteAllFrom("users", "ads", "messages", "message_threads", "message_thread_parties", "transactions", "communities");
  
  @BeforeClass
  public static void startUp() {
    new TestServer().start(new String[]{});
    awaitInitialization();
  }
  
  @BeforeClass
  public static void setupRestAssured() {
    RestAssured.port = 4567;
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
}
