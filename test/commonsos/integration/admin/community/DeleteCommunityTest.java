package commonsos.integration.admin.community;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
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

public class DeleteCommunityTest extends IntegrationTest {

  private Admin ncl;
  private Admin com1Admin1;
  private Admin com1Teller;
  private Community com1;
  private Community com2;
  private Community com3;
  private User user1;
  private User user2;
  private Ad com1Ad;
  private MessageThread com1AdThread;
  private MessageThread groupThread;
  private MessageThread directThread;
  private Redistribution red1;
  private Redistribution red2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setFee(ZERO).setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setFee(ZERO).setPublishStatus(PUBLIC));
    com3 =  create(new Community().setName("com3").setFee(ZERO).setPublishStatus(PRIVATE));
    
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setAdminname("ncl").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin1 = create(new Admin().setEmailAddress("com1Admin1@before.each.com").setAdminname("com1Admin1").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setAdminname("com1Teller").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
    
    user1 = create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2), new CommunityUser().setCommunity(com3))));
    user2 = create(new User().setUsername("user2").setCommunityUserList(asList(
        new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2), new CommunityUser().setCommunity(com3))));
    com1Ad = create(new Ad().setPhotoUrl("test").setCommunityId(com1.getId()));
    
    com1AdThread = create(new MessageThread().setAdId(com1Ad.getId()).setCommunityId(com1.getId()).setParties(asList(new MessageThreadParty().setUser(user1), new MessageThreadParty().setUser(user2))));
    groupThread = create(new MessageThread().setCommunityId(com1.getId()).setGroup(true)).setParties(asList(new MessageThreadParty().setUser(user1), new MessageThreadParty().setUser(user2)));
    directThread = create(new MessageThread().setCommunityId(com1.getId()).setParties(asList(new MessageThreadParty().setUser(user1), new MessageThreadParty().setUser(user2))));
    
    red1 = create(new Redistribution().setCommunity(com1).setAll(true).setRate(ONE));
    red2 = create(new Redistribution().setCommunity(com1).setUser(user1).setRate(ONE));
  }
  
  @Test
  public void deleteCommunity_ncl() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // delete com2
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/delete", com2.getId())
      .then().statusCode(200);
    
    // verify db
    User u = emService.get().find(User.class, user1.getId());
    assertThat(u.getCommunityUserList().size()).isEqualTo(2);
    assertThat(u.getCommunityUserList().stream().map(CommunityUser::getCommunity)).containsExactly(com1, com3);

    // delete com1
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/delete", com1.getId())
      .then().statusCode(200);

    // verify db
    Ad a = emService.get().find(Ad.class, com1Ad.getId());
    assertThat(a.isDeleted()).isTrue();
    
    MessageThread mt1 = emService.get().find(MessageThread.class, com1AdThread.getId());
    MessageThread mt2 = emService.get().find(MessageThread.class, groupThread.getId());
    MessageThread mt3 = emService.get().find(MessageThread.class, directThread.getId());
    assertThat(mt1.isDeleted()).isTrue();
    assertThat(mt2.isDeleted()).isTrue();
    assertThat(mt3.isDeleted()).isTrue();
    
    Redistribution r1 = emService.get().find(Redistribution.class, red1.getId());
    Redistribution r2 = emService.get().find(Redistribution.class, red2.getId());
    assertThat(r1.isDeleted()).isTrue();
    assertThat(r2.isDeleted()).isTrue();

    Admin ad1 = emService.get().find(Admin.class, com1Admin1.getId());
    Admin ad2 = emService.get().find(Admin.class, com1Teller.getId());
    assertThat(ad1.getCommunity()).isNull();
    assertThat(ad2.getCommunity()).isNull();
    
    Community c = emService.get().find(Community.class, com1.getId());
    emService.get().refresh(c);
    assertThat(c.isDeleted()).isTrue();
  }
  
  @Test
  public void deleteCommunity_com1Admin() throws Exception {
    sessionId = loginAdmin(com1Admin1.getEmailAddress(), "password");

    // delete community [normal case]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/delete", com1.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void deleteCommunity_com1Teller() throws Exception {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // delete community [normal case]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities/{id}/delete", com1.getId())
      .then().statusCode(403);
  }
}
