package commonsos.integration.app.transaction;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.NCL;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;

public class CreateTokenTransactionTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user;
  private User adCreator;
  private User otherCommunityUser;
  private Ad giveAd;
  private Ad wantAd;
  private Admin ncl;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setFee(BigDecimal.ONE).setPublishStatus(PUBLIC).setName("community"));
    otherCommunity =  create(new Community().setFee(BigDecimal.ONE).setPublishStatus(PUBLIC).setName("otherCommunity"));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    adCreator =  create(new User().setUsername("adCreator").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));
    giveAd =  create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(adCreator.getId()).setType(AdType.GIVE).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));
    wantAd =  create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(adCreator.getId()).setType(AdType.WANT).setCommunityId(community.getId()).setPoints(BigDecimal.TEN).setTitle("title"));

    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("pass")).setRole(NCL));

    sessionId = loginApp("user", "pass");
  }
  
  @Test
  public void transactionForAd_give() throws Exception {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", adCreator.getId()); // from user to adCreator
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "0");
    requestParam.put("amount", "9");
    requestParam.put("adId", giveAd.getId());
    
    // create token with no fee
    update(community.setFee(BigDecimal.ZERO));
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
    Thread.sleep(1000); // wait until multi thread is done.
    
    // verify db transaction
    TokenTransaction transaction = emService.get().createQuery("FROM TokenTransaction WHERE adId = :adId ORDER BY id DESC", TokenTransaction.class)
        .setParameter("adId", giveAd.getId())
        .setMaxResults(1).getSingleResult();
    assertThat(transaction.getCommunityId()).isEqualTo(community.getId());
    assertThat(transaction.getRemitterUserId()).isEqualTo(user.getId());
    assertThat(transaction.getBeneficiaryUserId()).isEqualTo(adCreator.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(9));
    assertThat(transaction.getDescription()).isEqualTo("description");
    assertThat(transaction.getFee()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(transaction.isRedistributed()).isFalse();

    // verify db transaction of fee
    Long countFeeTransaction = emService.get().createQuery("SELECT count(*) FROM TokenTransaction WHERE isFeeTransaction is true", Long.class).getSingleResult();
    assertThat(countFeeTransaction).isEqualTo(0);
    
    // verify db message
    Message message = emService.get().createQuery("FROM Message", Message.class).getSingleResult();
    assertThat(message.getText()).isEqualTo("userさんからadCreatorさんへ9 symbolを送信しました。\n【コメント】\ndescription");

    // create token with fee
    update(community.setFee(BigDecimal.ONE));
    requestParam.put("transactionFee", "1.0");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());

    // verify db transaction
    transaction = emService.get().createQuery("FROM TokenTransaction WHERE adId = :adId ORDER BY id DESC", TokenTransaction.class)
        .setParameter("adId", giveAd.getId())
        .setMaxResults(1).getSingleResult();
    assertThat(transaction.getFee()).isEqualByComparingTo(BigDecimal.ONE);
    
    // verify db transaction of fee
    TokenTransaction feeTransaction = emService.get().createQuery("FROM TokenTransaction WHERE isFeeTransaction is true ORDER BY id DESC", TokenTransaction.class)
        .setMaxResults(1).getSingleResult();
    assertThat(feeTransaction.getCommunityId()).isEqualTo(community.getId());
    assertThat(feeTransaction.getRemitterUserId()).isEqualTo(user.getId());
    assertThat(feeTransaction.getBeneficiaryUserId()).isNull();
    assertThat(feeTransaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.09));
    assertThat(feeTransaction.getFee()).isEqualByComparingTo(BigDecimal.ONE);
    assertThat(feeTransaction.getWalletDivision()).isEqualTo(WalletType.FEE);
    assertThat(feeTransaction.isRedistributed()).isFalse();
    
    // get transaction with admin
    sessionId = loginAdmin(ncl.getEmailAddress(), "pass");
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", community.getId(), "fee")
      .then().statusCode(200)
      .body("transactionList.amount",  contains(0.09F));
    
    // create transaction from adCreator
    sessionId = loginApp("adCreator", "pass");
    requestParam.put("beneficiaryId", user.getId()); // from adCreator to user
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
  }

  @Test
  public void transactionForAd_give_otherCommunityUser() {
    sessionId = loginApp("otherCommunityUser", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", otherCommunity.getId()); // with other community id
    requestParam.put("beneficiaryId", adCreator.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "1");
    requestParam.put("amount", "10");
    requestParam.put("adId", giveAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(468)
      .body("key", equalTo("error.beneficiaryIsNotCommunityMember"));

    // call api
    requestParam.put("communityId", community.getId()); // with right community id
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(468)
      .body("key", equalTo("error.userIsNotCommunityMember"));
  }

  @Test
  public void transactionForAd_want() {
    sessionId = loginApp("adCreator", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", user.getId()); // from adCreator to user
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "1");
    requestParam.put("amount", "9");
    requestParam.put("adId", wantAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
    
    // verify db
    TokenTransaction transaction = emService.get().createQuery("FROM TokenTransaction WHERE adId = :adId", TokenTransaction.class)
        .setParameter("adId", wantAd.getId())
        .getSingleResult();
    assertThat(transaction.getRemitterUserId()).isEqualTo(adCreator.getId());
    assertThat(transaction.getBeneficiaryUserId()).isEqualTo(user.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(9));
    assertThat(transaction.getDescription()).isEqualTo("description");
    
    // call api
    sessionId = loginApp("user", "pass");
    requestParam.put("beneficiaryId", adCreator.getId()); // from user to adCreator
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
  }

  @Test
  public void transactionForAd_want_wrongCommunityId() throws Exception {
    // user belong to same community
    update(user.setCommunityUserList(asList(new CommunityUser().setCommunity(community), new CommunityUser().setCommunity(otherCommunity))));
    update(adCreator.setCommunityUserList(asList(new CommunityUser().setCommunity(community), new CommunityUser().setCommunity(otherCommunity))));
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", otherCommunity.getId()); // with wrong community id
    requestParam.put("beneficiaryId", adCreator.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "1");
    requestParam.put("amount", "9");
    requestParam.put("adId", giveAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(400);

    // call api
    requestParam.put("communityId", community.getId()); // with right community id
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
  }

  @Test
  public void transactionForAd_want_otherCommunityUser() {
    sessionId = loginApp("adCreator", "pass");
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", otherCommunityUser.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "1");
    requestParam.put("amount", "10");
    requestParam.put("adId", wantAd.getId());
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(468)
      .body("key", equalTo("error.beneficiaryIsNotCommunityMember"));
  }

  @Test
  public void transactionBetweenUser() throws Exception {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", adCreator.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "1");
    requestParam.put("amount", "8.9");
    requestParam.put("adId", null);
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("communityId", equalTo(community.getId().intValue()))
      .body("balance", notNullValue());
    Thread.sleep(5000); // wait until multi thread is done.
    
    // verify db transaction
    TokenTransaction transaction = emService.get().createQuery("FROM TokenTransaction WHERE beneficiaryUserId = :userId", TokenTransaction.class)
        .setParameter("userId", adCreator.getId())
        .getSingleResult();
    assertThat(transaction.getRemitterUserId()).isEqualTo(user.getId());
    assertThat(transaction.getBeneficiaryUserId()).isEqualTo(adCreator.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("8.9"));
    assertThat(transaction.getDescription()).isEqualTo("description");
    assertThat(transaction.getAdId()).isNull();

    // verify db message
    Message message = emService.get().createQuery("FROM Message", Message.class).getSingleResult();
    assertThat(message.getText()).isEqualTo("userさんからadCreatorさんへ8.9 symbolを送信しました。\n【コメント】\ndescription");
  }

  @Test
  public void transactionBetweenUser_otherCommunityUser() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", otherCommunityUser.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "1");
    requestParam.put("amount", "10");
    requestParam.put("adId", null);
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(468)
      .body("key", equalTo("error.beneficiaryIsNotCommunityMember"));
  }

  @Test
  public void transaction_feeIncorrect() {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", adCreator.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", "99"); // incorrect fee
    requestParam.put("amount", "10");
    requestParam.put("adId", null);
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(468);
  }
}
