package commonsos.integration.blockchain;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static commonsos.repository.entity.WalletType.FEE;
import static commonsos.repository.entity.WalletType.MAIN;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;

public class BlockchainIntegrationTest extends IntegrationTest {
  
  private Logger log = Logger.getLogger(this.getClass().getName());

  private Admin ncl;
  private Admin comAdmin;
  private Admin comTeller;
  private Community community1;
  private Community community2;
  private User user1;
  private User user2;
  private String sessionId;

  @Override
  protected boolean isBlockchainEnable() { return true; }
  
  @Test
  public void testBlockchain() throws Exception {
    // create community
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("passpass")).setRole(NCL));
    community1 = createCommunity("community1", "c1", "community1");
    checkBalanceOfCommunity(community1, MAIN, greaterThan(BigInteger.valueOf(10 * 6)));
    checkBalanceOfCommunity(community1, FEE, equalTo(0));
    community2 = createCommunity("community2", "c2", "community2");
    comAdmin = create(new Admin().setEmailAddress("comAdmin@before.each.com").setPasswordHash(hash("passpass")).setRole(COMMUNITY_ADMIN));
    comTeller = create(new Admin().setEmailAddress("comTeller@before.each.com").setPasswordHash(hash("passpass")).setRole(TELLER));

    // create user
    user1 = createUser("user1_test", "passpass", "user1@test.com", true, asList(community1.getId()));
    user2 = createUser("user2_test", "passpass", "user2@test.com", true, asList(community1.getId(), community2.getId()));
    checkBalance(user1, community1, 0);
    checkBalance(user2, community1, 0);
    checkBalance(user2, community2, 0);

    // transfer token to user from admin
    transferTokenFromAdmin(community1, user1, 100);
    waitUntilTransactionCompleted(community1, user1, 100);
    checkBalance(user1, community1, 100);

    // transfer token to user from user
    transferToken(user1, user2, community1, 10, 90);
    waitUntilTransactionCompleted();
    checkBalance(user1, community1, 90);
    checkBalance(user2, community1, 10);
    
    // change a community of user
    changeCommunity(user1, asList(community2.getId()));
    waitUntilAllowed(user1, community2);
    
    // transfer token to user from admin
    transferTokenFromAdmin(community2, user1, 100);
    waitUntilTransactionCompleted(community2, user1, 100);
    checkBalance(user1, community2, 100);

    // transfer token to user from user
    transferToken(user1, user2, community2, 20, 80);
    waitUntilTransactionCompleted();
    checkBalance(user1, community2, 80);
    checkBalance(user2, community2, 20);

    // change a community of user
    changeCommunity(user1, asList(community1.getId(), community2.getId()));
    waitUntilTransactionCompleted();
    checkBalance(user1, community1, 90);
    checkBalance(user1, community2, 80);
    
    // get TokenBalance of main wallet
    checkBalanceOfCommunity(community1, MAIN, greaterThan(BigInteger.valueOf(10 * 6)));
    checkBalanceOfCommunity(community1, FEE, equalTo(0));
  }

  private User createUser(
      String username,
      String password,
      String emailAddress,
      boolean waitUntilCompleated,
      List<Long> communityList
      ) throws Exception {
    log.info(String.format("creating user started. [username=%s]", username));
    
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("username", username);
    requestParam.put("password", password);
    requestParam.put("emailAddress", emailAddress);
    requestParam.put("waitUntilCompleted", waitUntilCompleated);
    requestParam.put("communityList", communityList);
    
    // create temporary user
    given()
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/create-account", APP_API_VERSION.getMajor())
      .then().statusCode(200);
    
    // get accessId
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo(requestParam.get("emailAddress"));
    String accessId = extractAccessId(messages.get(0));
    messages.clear();
    
    // complete create user
    int userId = given()
      .when().post("/app/v{v}/create-account/{accessId}", APP_API_VERSION.getMajor(), accessId)
      .then().statusCode(200)
      .extract().path("id");

    log.info(String.format("creating user completed. [username=%s]", username));
    
    User user = emService.get().find(User.class, (long) userId);
    return user;
  }
  
  private void transferEtherToMainWallet(Community community) throws Exception {
    URL url = this.getClass().getResource("/blockchain/UTC--2018-11-08T09-10-38.833302600Z--766a1c4737970feddde6f2b8659fca05bd0339ab");
    Credentials commonsos = WalletUtils.loadCredentials("pass1", new File(url.toURI()));
    
    BigInteger initialEtherAmountForAdmin = BigInteger.TEN.pow(20);
    blockchainService.transferEther(commonsos, community.getMainWalletAddress(), initialEtherAmountForAdmin);
  }
  
  private Community createCommunity(
      String communityName,
      String tokenSymbol,
      String tokenName) throws Exception {
    log.info(String.format("creating community started. [communityName=%s]", communityName));

    sessionId = loginAdmin(ncl.getEmailAddress(), "passpass");
    
    // create community
    int id = given()
      .multiPart("communityName", communityName)
      .multiPart("tokenName", tokenName)
      .multiPart("tokenSymbol", tokenSymbol)
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200)
      .body("communityName", equalTo(communityName))
      .body("tokenName", equalTo(tokenName))
      .body("tokenSymbol", equalTo(tokenSymbol))
      .extract().path("communityId");
    
    Community community = emService.get().find(Community.class, (long) id);
    
    update(community.setStatus(PUBLIC));
    log.info(String.format("creating community completed. [communityName=%s]", communityName));

    log.info(String.format("transfer ether to main wallet started. [main wallet=%s]", community.getMainWalletAddress()));
    transferEtherToMainWallet(community);
    log.info(String.format("transfer ether to main wallet completed. [username=%s]", community.getMainWalletAddress()));
    
    return community;
  }

  private void transferTokenFromAdmin(Community community, User to, int amount) {
    blockchainService.transferTokensFromCommunity(community, MAIN, to, BigDecimal.valueOf(amount));
  }

  private void transferToken(User from, User to, Community community, int amount, float expectAmount) {
    Matcher<Float> matcher = equalTo(expectAmount);
    transferToken(from, to, community, amount, matcher);
  }

  private void transferToken(User from, User to, Community community, int amount, Matcher<?> expectAmount) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", to.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", 0);
    requestParam.put("amount", amount);
    requestParam.put("adId", null);
    log.info(String.format("transfer token started. [from=%s, to=%s, community=%s]", from.getUsername(), to.getUsername(), community.getName()));
    
    sessionId = loginApp(from.getUsername(), "passpass");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/transactions", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("balance", expectAmount);
    log.info(String.format("transfer token completed. [from=%s, to=%s, community=%s]", from.getUsername(), to.getUsername(), community.getName()));
  }
  
  private void checkBalance(User user, Community community, int expected) {
    sessionId = loginApp(user.getUsername(), "passpass");
    given().cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/balance?communityId={communityId}", APP_API_VERSION.getMajor(), community.getId())
      .then().statusCode(200).body("balance", equalTo(expected));
    log.info(String.format("check balance ok. [user=%s, community=%s, balance=%d]", user.getUsername(), community.getName(), expected));
  }
  
  private void changeCommunity(User user, List<Long> communityList) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityList", communityList);
    log.info(String.format("change community started. [user=%s]", user.getUsername()));
    
    sessionId = loginApp(user.getUsername(), "passpass");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/users/{id}/communities", APP_API_VERSION.getMajor(), user.getId())
      .then().statusCode(200);
    log.info(String.format("change community completed. [user=%s]", user.getUsername()));
  }
  
  private void checkBalanceOfCommunity(Community com, WalletType walletType, Matcher<?> expect) {
    sessionId = loginAdmin(ncl.getEmailAddress(), "passpass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin/balance?communityId={id}&wallet={wallet}", com.getId(), walletType.name())
      .then().statusCode(200)
      .body("balance", expect)
      .extract().path("balance");
  }

  private void waitUntilTransactionCompleted() throws Exception {
    log.info(String.format("waiting for transaction."));
    for (int i = 0; i < 60; i++) {
      long count = emService.get().createQuery(
          "SELECT count(t) FROM TokenTransaction t WHERE blockchainCompletedAt is null", Long.class)
          .getSingleResult();
      if(count == 0) {
        log.info(String.format("transaction completed."));
        return;
      }
      Thread.sleep(1000);
    }
    throw new RuntimeException("transaction didn't finish in 60 seconds.");
  }
  
  private void waitUntilTransactionCompleted(Community community, User user, int expected) throws Exception {
    log.info(String.format("waiting for transaction."));
    sessionId = loginApp(user.getUsername(), "passpass");

    for (int i = 0; i < 60; i++) {
      int balanceInt = given()
        .cookie("JSESSIONID", sessionId)
        .when().get("/app/v{v}/balance?communityId={id}", APP_API_VERSION.getMajor(), community.getId())
        .then().statusCode(200)
        .extract().path("balance");
      BigDecimal balance = BigDecimal.valueOf(balanceInt);
      if(balance.compareTo(BigDecimal.valueOf(expected)) == 0) {
        log.info(String.format("transaction completed."));
        return;
      }
      Thread.sleep(1000);
    }
    throw new RuntimeException("transaction didn't finish in 60 seconds.");
  }
  
  private void waitUntilAllowed(User user, Community community) throws Exception {
    log.info(String.format("waiting for allowing."));
    for (int i = 0; i < (60*30); i++) {
      if (blockchainService.isAllowed(user, community, BigInteger.ONE)) {
        log.info(String.format("allowing completed."));
        return;
      }
      Thread.sleep(1000);
    }
    throw new RuntimeException("allowence didn't finish in 30 minutes.");
  }
}
