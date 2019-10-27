package commonsos.integration.admin.user;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.User;

public class DeleteUserTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Admin com2Admin;
  private User com1User;
  private User com1com2User;
  private User nonComUser;
  private Ad com1com2UserAd1;
  private Ad com1com2UserAd2;
  private Redistribution com1com2UserRed1;
  private Redistribution com1com2UserRed2;
  private Redistribution com1com2UserRed3;
  private MessageThread messageThread1;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com2Admin = create(new Admin().setEmailAddress("com2Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com2));
    
    com1User = create(new User().setUsername("com1User").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1com2User = create(new User().setUsername("com1com2User").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    nonComUser = create(new User().setUsername("nonComUser").setCommunityUserList(asList()));
    
    com1com2UserAd1 = create(new Ad().setCommunityId(com1.getId()).setCreatedUserId(com1com2User.getId()));
    com1com2UserAd2 = create(new Ad().setCommunityId(com2.getId()).setCreatedUserId(com1com2User.getId()));
    com1com2UserRed1 = create(new Redistribution().setCommunity(com1).setUser(com1com2User));
    com1com2UserRed2 = create(new Redistribution().setCommunity(com2).setUser(com1com2User));
    com1com2UserRed3 = create(new Redistribution().setCommunity(com2).setAll(true));
    messageThread1 = create(new MessageThread().setCommunityId(com1.getId()).setParties(asList(
            new MessageThreadParty().setUser(com1User),
            new MessageThreadParty().setUser(com1com2User))));
  }
  
  @Test
  public void deleteUser_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // delete user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", com1com2User.getId())
      .then().statusCode(200);
    
    // verify db
    assertThat(emService.get().find(User.class, com1com2User.getId()).isDeleted()).isEqualTo(true);
    assertThat(emService.get().find(Ad.class, com1com2UserAd1.getId()).isDeleted()).isEqualTo(true);
    assertThat(emService.get().find(Ad.class, com1com2UserAd2.getId()).isDeleted()).isEqualTo(true);
    assertThat(emService.get().find(Redistribution.class, com1com2UserRed1.getId()).isDeleted()).isEqualTo(true);
    assertThat(emService.get().find(Redistribution.class, com1com2UserRed2.getId()).isDeleted()).isEqualTo(true);
    assertThat(emService.get().find(Redistribution.class, com1com2UserRed3.getId()).isDeleted()).isEqualTo(false);
    MessageThread mt1 = emService.get().find(MessageThread.class, messageThread1.getId());
    assertThat(mt1.getParties()).extracting(MessageThreadParty::getUser).containsExactly(com1User);

    // delete user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", nonComUser.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void deleteUser_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // delete user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", com1User.getId())
      .then().statusCode(200);

    // delete user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", com1com2User.getId())
      .then().statusCode(200);

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", nonComUser.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void deleteUser_byCom2Admin() {
    sessionId = loginAdmin(com2Admin.getEmailAddress(), "password");

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", com1User.getId())
      .then().statusCode(403);

    // delete user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", com1com2User.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void deleteUser_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // delete user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", com1User.getId())
      .then().statusCode(200);

    // delete user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", com1com2User.getId())
      .then().statusCode(200);

    // forbidden
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/users/{id}/delete", nonComUser.getId())
      .then().statusCode(403);
  }
}
