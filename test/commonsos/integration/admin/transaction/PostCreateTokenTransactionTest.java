package commonsos.integration.admin.transaction;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PRIVATE;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static commonsos.repository.entity.WalletType.FEE;
import static commonsos.repository.entity.WalletType.MAIN;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.util.MessageUtil;

public class PostCreateTokenTransactionTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User user_com1;
  private User user_com2;
  private String sessionId;

  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC).setFee(ONE));
    com2 =  create(new Community().setName("com2").setStatus(PRIVATE).setFee(ONE));

    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
    
    user_com1 =  create(new User().setUsername("user_com1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    user_com2 =  create(new User().setUsername("user_com2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(com2))));
  }

  @Test
  public void createTransaction_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // send token from main wallet
    Map<String, Object> requestParam = getRequestParam(com1, "MAIN", user_com1, 10);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/coin")
      .then().statusCode(200);

    // verify db transaction
    TokenTransaction transaction = emService.get().createQuery("FROM TokenTransaction ORDER BY id DESC", TokenTransaction.class).setMaxResults(1).getSingleResult();
    assertThat(transaction.getCommunityId()).isEqualTo(com1.getId());
    assertThat(transaction.getRemitterUserId()).isNull();
    assertThat(transaction.isFromAdmin()).isTrue();
    assertThat(transaction.getRemitterAdminId()).isEqualTo(ncl.getId());
    assertThat(transaction.getWalletDivision()).isEqualTo(MAIN);
    assertThat(transaction.getBeneficiaryUserId()).isEqualTo(user_com1.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(TEN);
    assertThat(transaction.getFee()).isEqualByComparingTo(ZERO);
    assertThat(transaction.isRedistributed()).isTrue();

    // verify db message
    Message message = emService.get().createQuery("FROM Message ORDER BY id DESC", Message.class).setMaxResults(1).getSingleResult();
    assertThat(message.getText()).isEqualTo("com1からuser_com1さんへ10 symbolを送信しました。");

    // verify db message_thread
    MessageThread messageThread = emService.get().find(MessageThread.class, message.getThreadId());
    messageThread.getParties().sort((a,b) -> a.getId().compareTo(b.getId()));
    assertThat(messageThread.getParties().get(0).getUser().getId()).isEqualTo(user_com1.getId());
    assertThat(messageThread.getParties().get(1).getUser().getId()).isEqualTo(MessageUtil.getSystemMessageCreatorId());
    
    // send token from fee wallet
    requestParam = getRequestParam(com1, "FEE", user_com1, 10);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/coin")
      .then().statusCode(200);

    // verify db transaction
    transaction = emService.get().createQuery("FROM TokenTransaction ORDER BY id DESC", TokenTransaction.class).setMaxResults(1).getSingleResult();
    assertThat(transaction.getWalletDivision()).isEqualTo(FEE);

    // get message_thread of system message
    sessionId = loginApp(user_com1.getUsername(), "pass");
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/message-threads?communityId={communityId}", APP_API_VERSION.getMajor(), com1.getId())
      .then().statusCode(200)
      .body("messageThreadList.id", contains(messageThread.getId().intValue()))
      .body("messageThreadList.parties.id", contains(asList(MessageUtil.getSystemMessageCreatorId().intValue())))
      .body("messageThreadList.parties.username", contains(asList("SYSTEM")))
      .body("messageThreadList.creator.id", contains(user_com1.getId().intValue()))
      .body("messageThreadList.counterParty.id", contains(MessageUtil.getSystemMessageCreatorId().intValue()));
  }

  @Test
  public void createTransaction_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // send to user_com1
    Map<String, Object> requestParam = getRequestParam(com1, "MAIN", user_com1, 10);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/coin")
      .then().statusCode(200);

    // send to user_com2
    requestParam = getRequestParam(com2, "MAIN", user_com1, 10);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/coin")
      .then().statusCode(468);
  }

  @Test
  public void createTransaction_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // send to user_com1
    Map<String, Object> requestParam = getRequestParam(com1, "MAIN", user_com1, 10);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/coin")
      .then().statusCode(403);

    // send to user_com2
    requestParam = getRequestParam(com2, "MAIN", user_com1, 10);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/coin")
      .then().statusCode(468);
  }

  private Map<String, Object> getRequestParam(Community com, String wallet, User beneficiary, Integer amount) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", com.getId());
    requestParam.put("wallet", wallet);
    requestParam.put("beneficiaryUserId", beneficiary.getId());
    requestParam.put("amount", amount);
    return requestParam;
  }
}
