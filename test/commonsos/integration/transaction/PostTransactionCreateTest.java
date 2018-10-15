package commonsos.integration.transaction;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdType;
import commonsos.repository.community.Community;
import commonsos.repository.transaction.Transaction;
import commonsos.repository.user.User;

public class PostTransactionCreateTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user;
  private User adCreator;
  private User otherCommunityUser;
  private Ad giveAd;
  private Ad wantAd;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    otherCommunity =  create(new Community().setName("otherCommunity"));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community)));
    adCreator =  create(new User().setUsername("adCreator").setPasswordHash(hash("pass")).setJoinedCommunities(asList(community)));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setJoinedCommunities(asList(otherCommunity)));
    giveAd =  create(new Ad().setCreatedBy(adCreator.getId()).setType(AdType.GIVE).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));
    wantAd =  create(new Ad().setCreatedBy(adCreator.getId()).setType(AdType.WANT).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));

    sessionId = login("user", "pass");
  }
  
  @Test
  public void transactionForAd_give() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", adCreator.getId());
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
  }

  @Test
  public void transactionForAd_give_otherCommunityUser() {
    sessionId = login("otherCommunityUser", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", otherCommunity.getId());
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
  }

  @Test
  public void transactionForAd_want() {
    sessionId = login("adCreator", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", user.getId());
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
      .then().statusCode(400);
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
      .then().statusCode(200);
    
    // verify db
    Transaction transaction = emService.get().createQuery("FROM Transaction WHERE beneficiaryId = :userId", Transaction.class)
        .setParameter("userId", otherCommunityUser.getId())
        .getSingleResult();
    assertThat(transaction.getRemitterId()).isEqualTo(user.getId());
    assertThat(transaction.getBeneficiaryId()).isEqualTo(otherCommunityUser.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(transaction.getDescription()).isEqualTo("description");
    assertThat(transaction.getAdId()).isNull();
  }
}
