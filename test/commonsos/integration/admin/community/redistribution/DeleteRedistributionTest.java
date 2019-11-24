package commonsos.integration.admin.community.redistribution;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.User;

public class DeleteRedistributionTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User com1user1;
  private User com2user1;
  private Redistribution com1r1;
  private Redistribution com2r1;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com1user1 = create(new User().setUsername("com1user1").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com2user1 = create(new User().setUsername("com2user1").setCommunityUserList(asList(new CommunityUser().setCommunity(com2))));

    com1r1 = create(new Redistribution().setCommunity(com1).setUser(com1user1).setRate(new BigDecimal("5")));
    com2r1 = create(new Redistribution().setCommunity(com2).setUser(com1user1).setRate(new BigDecimal("1.5")));
  }
  
  @Test
  public void deleteRedistribution_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // get redistribution
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions/{rId}", com1.getId(), com1r1.getId())
      .then().statusCode(200);

    // delete redistribution
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}/delete", com1.getId(), com1r1.getId())
      .then().statusCode(200);

    // get redistribution
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions/{rId}", com1.getId(), com1r1.getId())
      .then().statusCode(400);
  }
  
  @Test
  public void deleteRedistribution_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // delete redistribution
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}/delete", com1.getId(), com1r1.getId())
      .then().statusCode(200);
    
    // delete redistribution [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}/delete", com2.getId(), com2r1.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void deleteRedistribution_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // delete redistribution
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}/delete", com1.getId(), com1r1.getId())
      .then().statusCode(403);
    
    // delete redistribution [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/redistributions/{rId}/delete", com2.getId(), com2r1.getId())
      .then().statusCode(403);
  }
}
