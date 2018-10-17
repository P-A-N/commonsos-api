package commonsos.integration.user;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.community.Community;
import commonsos.repository.user.User;

public class PostProvisionalEmailUpdateTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  private User user;
  private String sessionId;
  
  @Before
  public void createUser() {
    user = create(new User().setUsername("user").setPasswordHash(hash("pass")));
    community1 =  create(new Community().setName("community1"));
    community2 =  create(new Community().setName("community2"));
    community3 =  create(new Community().setName("community3"));

    sessionId = login("user", "pass");
  }
  
  @Test
  public void provisionalEmailUpdate() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("newEmailAddress", "test@test.com");

    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{:id}/emailaddress", 123)
      .then().statusCode(200);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/users/{:id}/emailaddress", 123)
      .then().statusCode(400);

    given()
      .body(gson.toJson(requestParam))
      .when().post("/users/{:id}/emailaddress", 123)
      .then().statusCode(401);
  }
}
