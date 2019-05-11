package commonsos.integration.ad;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.User;

public class PostAdDeleteTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User admin1;
  private User admin2;
  private User user1;
  private User user2;
  private Ad ad_user1;
  private MessageThread messageThread_ad_user1;
  private String sessionId;
  
  @BeforeEach
  public void setupData() {
    community1 =  create(new Community().setName("community1"));
    community2 =  create(new Community().setName("community2"));
    admin1 =  create(new User().setUsername("admin1").setPasswordHash(hash("pass")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1), new CommunityUser().setCommunity(community2))));
    admin2 =  create(new User().setUsername("admin2").setPasswordHash(hash("pass")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1), new CommunityUser().setCommunity(community2))));
    update(community1.setAdminUser(admin1));
    update(community2.setAdminUser(admin2));
    
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1), new CommunityUser().setCommunity(community2))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1), new CommunityUser().setCommunity(community2))));
    
    ad_user1 =  create(new Ad().setCreatedBy(user1.getId()).setCommunityId(community1.getId()));
    messageThread_ad_user1 = create(new MessageThread().setAdId(ad_user1.getId()).setCommunityId(ad_user1.getCommunityId()));
  }
  
  @Test
  public void adDelete_byUser() {
    // delete by non creator
    sessionId = login("user2", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/ads/{id}/delete", ad_user1.getId())
      .then().statusCode(403);
    
    // verify
    Ad actual = emService.get().find(Ad.class, ad_user1.getId());
    assertThat(actual.isDeleted()).isFalse();

    // delete by creator
    sessionId = login("user1", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/ads/{id}/delete", ad_user1.getId())
      .then().statusCode(200);
    
    // verify
    emService.get().refresh(actual);
    assertThat(actual.isDeleted()).isTrue();
    MessageThread mt = emService.get().find(MessageThread.class, messageThread_ad_user1.getId());
    assertThat(mt.isDeleted()).isTrue();
  }

  
  @Test
  public void adDelete_byAdmin() {
    // delete by creator
    sessionId = login("user1", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad_user1.getId())
      .then().statusCode(403);
    
    // verify
    Ad actual = emService.get().find(Ad.class, ad_user1.getId());
    assertThat(actual.isDeleted()).isFalse();

    // delete by other admin
    sessionId = login("admin2", "pass");

    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad_user1.getId())
      .then().statusCode(403);
    
    // verify
    emService.get().refresh(actual);
    assertThat(actual.isDeleted()).isFalse();
    
    // delete by admin
    sessionId = login("admin1", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/ads/{id}/delete", ad_user1.getId())
      .then().statusCode(200);
    
    // verify
    emService.get().refresh(actual);
    assertThat(actual.isDeleted()).isTrue();
    MessageThread mt = emService.get().find(MessageThread.class, messageThread_ad_user1.getId());
    assertThat(mt.isDeleted()).isTrue();
  }
}
