package commonsos.integration.admin.community.redistribution;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.User;

public class GetRedistributionTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User user1;
  private Redistribution r1;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    user1 = create(new User().setUsername("user1").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));

    r1 = create(new Redistribution().setCommunity(com1).setUser(user1).setRate(new BigDecimal("1.5")));
  }
  
  @Test
  public void createRedistribution_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // get redistribution [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions/{rId}", com1.getId(), r1.getId())
      .then().statusCode(200)
      .body("isAll", equalTo(false))
      .body("userId", equalTo(user1.getId().intValue()))
      .body("username", equalTo("user1"))
      .body("redistributionRate", equalTo(1.5F));
    
    // get redistribution [wrong community]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions/{rId}", com2.getId(), r1.getId())
      .then().statusCode(400);
  }
  
  @Test
  public void createRedistribution_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    // get redistribution [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions/{rId}", com1.getId(), r1.getId())
      .then().statusCode(200)
      .body("isAll", equalTo(false))
      .body("userId", equalTo(user1.getId().intValue()))
      .body("username", equalTo("user1"))
      .body("redistributionRate", equalTo(1.5F));
    
    // get redistribution [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions/{rId}", com2.getId(), r1.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void createRedistribution_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    // get redistribution [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions/{rId}", com1.getId(), r1.getId())
      .then().statusCode(403);
  }
}
