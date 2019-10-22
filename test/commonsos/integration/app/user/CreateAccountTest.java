package commonsos.integration.app.user;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class CreateAccountTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User admin;
  
  @BeforeEach
  public void createUser() throws Exception {
    admin = create(new User().setUsername("admin"));
    community1 =  create(new Community().setName("community1").setAdminUser(admin).setPublishStatus(PUBLIC));
    community2 =  create(new Community().setName("community2").setAdminUser(admin).setPublishStatus(PUBLIC));
  }
  
  @Test
  public void createAccount_multipleCommunity() throws Exception {
    // createAccountTemporary
    Map<String, Object> createAccountParam = getCreateAccountParam();
    given()
      .body(gson.toJson(createAccountParam))
      .when().post("/app/v{v}/create-account", APP_API_VERSION.getMajor())
      .then().statusCode(200);
    
    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo(createAccountParam.get("emailAddress"));
    String accessId = extractAccessId(messages.get(0));

    // login should fail at now
    Map<String, Object> loginParam = getLoginParam();
    given()
      .body(gson.toJson(loginParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(401);
    
    // createAccountComplete
    given()
      .when().post("/app/v{v}/create-account/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(200)
      .body("loggedinAt", notNullValue());

    // login should success
    given()
      .body(gson.toJson(loginParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityList.name", contains("community1", "community2"));
    
    // check if accessId is invalid
    given()
      .when().post("/app/v{v}/create-account/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(400);

    // username already taken
    Map<String, Object> createAccountParam2 = getCreateAccountParam();
    createAccountParam2.put("emailAddress", "test2@test.com");
    given()
      .body(gson.toJson(createAccountParam2))
      .when().post("/app/v{v}/create-account", APP_API_VERSION.getMajor())
      .then().statusCode(468);

    // email address already taken
    Map<String, Object> createAccountParam3 = getCreateAccountParam();
    createAccountParam3.put("username", "user2");
    given()
      .body(gson.toJson(createAccountParam3))
      .when().post("/app/v{v}/create-account", APP_API_VERSION.getMajor())
      .then().statusCode(468);
  }
  
  @Test
  public void createAccount_SingleCommunity() throws Exception {
    Map<String, Object> createAccountParam = getCreateAccountParam();
    createAccountParam.put("communityList", new ArrayList<Long>(Arrays.asList(community1.getId())));

    given()
      .body(gson.toJson(createAccountParam))
      .when().post("/app/v{v}/create-account", APP_API_VERSION.getMajor())
      .then().statusCode(200);
    
    String accessId = extractAccessId(wiser.getMessages().get(0));
    Map<String, Object> loginParam = getLoginParam();
    given()
      .when().post("/app/v{v}/create-account/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(200)
      .body("loggedinAt", notNullValue());

    // login should success
    given()
      .body(gson.toJson(loginParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityList.name", contains("community1"));
  }
  
  @Test
  public void createAccount_ZeroCommunity() throws Exception {
    Map<String, Object> createAccountParam = getCreateAccountParam();
    createAccountParam.put("communityList", new ArrayList<Long>());

    given()
      .body(gson.toJson(createAccountParam))
      .when().post("/app/v{v}/create-account", APP_API_VERSION.getMajor())
      .then().statusCode(200);
    
    String accessId = extractAccessId(wiser.getMessages().get(0));
    Map<String, Object> loginParam = getLoginParam();
    given()
      .when().post("/app/v{v}/create-account/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(200)
      .body("loggedinAt", notNullValue());

    // login should success
    given()
      .body(gson.toJson(loginParam))
      .when().post("/app/v{v}/login", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityList.name", empty());
  }
  
  private Map<String, Object> getCreateAccountParam() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "user");
    requestParam.put("password", "password");
    requestParam.put("firstName", "firstName");
    requestParam.put("lastName", "lastName");
    requestParam.put("description", "description");
    requestParam.put("location", "location");
    requestParam.put("emailAddress", "test@test.com");
    requestParam.put("telNo", "000-0000-0000");
    requestParam.put("waitUntilCompleted", false);
    List<Long> communityList = new ArrayList<Long>(Arrays.asList(community1.getId(), community2.getId()));
    requestParam.put("communityList", communityList);
    
    return requestParam;
  }
  
  private Map<String, Object> getLoginParam() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", "user");
    requestParam.put("password", "password");
    
    return requestParam;
  }
}
