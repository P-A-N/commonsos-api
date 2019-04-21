package commonsos.integration.transaction;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Transaction;
import commonsos.repository.entity.User;

public class PostTransactionCreateTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user;
  private User adCreator;
  private User otherCommunityUser;
  private Ad giveAd;
  private Ad wantAd;
  private String sessionId;
  
  @BeforeEach
  public void setup() {
    community =  create(new Community().setName("community"));
    otherCommunity =  create(new Community().setName("otherCommunity"));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    adCreator =  create(new User().setUsername("adCreator").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));
    giveAd =  create(new Ad().setCreatedBy(adCreator.getId()).setType(AdType.GIVE).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));
    wantAd =  create(new Ad().setCreatedBy(adCreator.getId()).setType(AdType.WANT).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));

    sessionId = login("user", "pass");
  }
  
  @Test
  public void transactionForAd_give() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", adCreator.getId()); // from user to adCreator
    requestParam.put("description", "description");
    requestParam.put("amount", "10");
    requestParam.put("adId", giveAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(200);
    
    // verify db
    Transaction transaction = emService.get().createQuery("FROM Transaction WHERE adId = :adId", Transaction.class)
        .setParameter("adId", giveAd.getId())
        .getSingleResult();
    assertThat(transaction.getRemitterId()).isEqualTo(user.getId());
    assertThat(transaction.getBeneficiaryId()).isEqualTo(adCreator.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(transaction.getDescription()).isEqualTo("description");
    
    // call api
    sessionId = login("adCreator", "pass");
    requestParam.put("beneficiaryId", user.getId()); // from adCreator to user
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(200);
  }

  @Test
  public void transactionForAd_give_otherCommunityUser() {
    sessionId = login("otherCommunityUser", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", otherCommunity.getId()); // with other community id
    requestParam.put("beneficiaryId", adCreator.getId());
    requestParam.put("description", "description");
    requestParam.put("amount", "10");
    requestParam.put("adId", giveAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(468)
      .body("key", equalTo("error.beneficiaryIsNotCommunityMember"));

    // call api
    requestParam.put("communityId", community.getId()); // with right community id
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(468)
      .body("key", equalTo("error.userIsNotCommunityMember"));

  }

  @Test
  public void transactionForAd_want() {
    sessionId = login("adCreator", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", user.getId()); // from adCreator to user
    requestParam.put("description", "description");
    requestParam.put("amount", "10");
    requestParam.put("adId", wantAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(200);
    
    // verify db
    Transaction transaction = emService.get().createQuery("FROM Transaction WHERE adId = :adId", Transaction.class)
        .setParameter("adId", wantAd.getId())
        .getSingleResult();
    assertThat(transaction.getRemitterId()).isEqualTo(adCreator.getId());
    assertThat(transaction.getBeneficiaryId()).isEqualTo(user.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(transaction.getDescription()).isEqualTo("description");
    
    // call api
    sessionId = login("user", "pass");
    requestParam.put("beneficiaryId", adCreator.getId()); // from user to adCreator
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(200);
  }

  @Test
  public void transactionForAd_want_wrongCommunityId() {
    // user belong to same community
    update(user.setCommunityUserList(asList(new CommunityUser().setCommunity(community), new CommunityUser().setCommunity(otherCommunity))));
    update(adCreator.setCommunityUserList(asList(new CommunityUser().setCommunity(community), new CommunityUser().setCommunity(otherCommunity))));
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", otherCommunity.getId()); // with wrong community id
    requestParam.put("beneficiaryId", adCreator.getId());
    requestParam.put("description", "description");
    requestParam.put("amount", "10");
    requestParam.put("adId", giveAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(400);

    // call api
    requestParam.put("communityId", community.getId()); // with right community id
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(200);
  }

  @Test
  public void transactionForAd_want_otherCommunityUser() {
    sessionId = login("adCreator", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", otherCommunityUser.getId());
    requestParam.put("description", "description");
    requestParam.put("amount", "10");
    requestParam.put("adId", wantAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(468)
      .body("key", equalTo("error.beneficiaryIsNotCommunityMember"));
  }

  @Test
  public void transactionBetweenUser() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", adCreator.getId());
    requestParam.put("description", "description");
    requestParam.put("amount", "10");
    requestParam.put("adId", null);
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(200);
    
    // verify db
    Transaction transaction = emService.get().createQuery("FROM Transaction WHERE beneficiaryId = :userId", Transaction.class)
        .setParameter("userId", adCreator.getId())
        .getSingleResult();
    assertThat(transaction.getRemitterId()).isEqualTo(user.getId());
    assertThat(transaction.getBeneficiaryId()).isEqualTo(adCreator.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(transaction.getDescription()).isEqualTo("description");
    assertThat(transaction.getAdId()).isNull();
  }

  @Test
  public void transactionBetweenUser_otherCommunityUser() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", otherCommunityUser.getId());
    requestParam.put("description", "description");
    requestParam.put("amount", "10");
    requestParam.put("adId", null);
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(468)
      .body("key", equalTo("error.beneficiaryIsNotCommunityMember"));
  }
}
