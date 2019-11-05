package commonsos.integration.admin.admin;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class UpdateAdminEmailAddressTest extends IntegrationTest {

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

    // update ncl1 email address temporary
    Map<String, Object> requestParam = getRequestParam("updated@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", ncl1.getId())
      .then().statusCode(200);

    // verify email
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo("updated@test.com");
    String[] tmp = extractAccessId(messages.get(0), "#").split("-");
    String adminId = tmp[0];
    String accessId = tmp[1];

    // email address is taken
    requestParam = getRequestParam("updated@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Admin.getId());
    
    // login filed
    failLoginAdmin("updated@test.com", "password");

    // update ncl1 email address temporary
    given()
      .when().post("/admin/admins/{id}/emailaddress/{accessId}", adminId, accessId)
      .then().statusCode(200);

    // login success
    loginAdmin("updated@test.com", "password");

    // email address is taken
    requestParam = getRequestParam("updated@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Admin.getId());
    
    // update ncl2 forbidden
    requestParam = getRequestParam("updated10@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", ncl2.getId())
      .then().statusCode(403);
    
    // update com1Admin
    requestParam = getRequestParam("updated11@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Admin.getId())
      .then().statusCode(200);
    
    // update com1Teller
    requestParam = getRequestParam("updated12@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Teller.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateAdmin_com1Admin() throws Exception {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // update ncl1
    Map<String, Object> requestParam = getRequestParam("updated1@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", ncl1.getId())
      .then().statusCode(403);

    // update com1Admin
    requestParam = getRequestParam("updated2@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Admin.getId())
      .then().statusCode(200);

    // update com1Teller
    requestParam = getRequestParam("updated3@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Teller.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void updateAdmin_com1Teller() throws Exception {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // update ncl1
    Map<String, Object> requestParam = getRequestParam("updated1@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", ncl1.getId())
      .then().statusCode(403);

    // update com1Admin
    requestParam = getRequestParam("updated2@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Admin.getId())
      .then().statusCode(403);

    // update com1Teller
    requestParam = getRequestParam("updated3@test.com");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/admins/{id}/emailaddress", com1Teller.getId())
      .then().statusCode(200);
  }

  private Map<String, Object> getRequestParam(String newEmailAddress) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("newEmailAddress", newEmailAddress);
    return requestParam;
  }
}
