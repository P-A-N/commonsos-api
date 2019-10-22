package commonsos.integration.blockchain;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.WalletType.FEE;
import static commonsos.repository.entity.WalletType.MAIN;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;

public class BlockchainIntegrationTest extends IntegrationTest {
  
  private Logger log = Logger.getLogger(this.getClass().getName());

  private Admin ncl;
  private Admin com1Admin;
  private Admin com2Admin;
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
    ncl = create(new Admin().setEmailAddress("ncl@test.com").setPasswordHash(hash("passpass")).setRole(NCL));
    community1 = createCommunity("community1", "c1", "community1");
    checkTokenBalanceOfCommunity(community1, MAIN, greaterThan(BigInteger.valueOf(10 * 6)));
    checkTokenBalanceOfCommunity(community1, FEE, equalTo(0));

    community2 = createCommunity("community2", "c2", "community2");
    checkTokenBalanceOfCommunity(community2, MAIN, greaterThan(BigInteger.valueOf(10 * 6)));
    checkTokenBalanceOfCommunity(community2, FEE, equalTo(0));
    
    com1Admin = create(new Admin().setEmailAddress("com1Admin@test.com").setPasswordHash(hash("passpass")).setRole(COMMUNITY_ADMIN).setCommunity(community1));
    com2Admin = create(new Admin().setEmailAddress("com2Admin@test.com").setPasswordHash(hash("passpass")).setRole(COMMUNITY_ADMIN).setCommunity(community2));

    // create user
    user1 = createUser("user1_test", "passpass", "user1@test.com", true, asList(community1.getId()));
    user2 = createUser("user2_test", "passpass", "user2@test.com", true, asList(community1.getId(), community2.getId()));
    checkBalanceOfUser(user1, community1, equalTo(0));
    checkBalanceOfUser(user2, community1, equalTo(0));
    checkBalanceOfUser(user2, community2, equalTo(0));

    // transfer token to user from admin (main)
    transferTokenFromAdmin(com1Admin, user1, MAIN, 1000);
    waitUntilTokenTransactionCompleted();
    checkBalanceOfUser(user1, community1, equalTo(1000));

    // transfer token to user from user
    transferTokenFromUser(user1, user2, community1, 100, equalTo(899F));
    waitUntilTokenTransactionCompleted();
    checkBalanceOfUser(user1, community1, equalTo(899));
    checkBalanceOfUser(user2, community1, equalTo(100));
    checkTokenBalanceOfCommunity(community1, FEE, equalTo(1));
    
    // change a community of user
    changeCommunity(user1, asList(community2.getId()));
    waitUntilAllowed(user1, community2);
    
    // transfer token to user from admin (main)
    transferTokenFromAdmin(com2Admin, user1, MAIN, 1000);
    waitUntilTokenTransactionCompleted();
    checkBalanceOfUser(user1, community2, equalTo(1000));

    // transfer token to user from user
    transferTokenFromUser(user1, user2, community2, 200, equalTo(798F));
    waitUntilTokenTransactionCompleted();
    checkBalanceOfUser(user1, community2, equalTo(798));
    checkBalanceOfUser(user2, community2, equalTo(200));
    checkTokenBalanceOfCommunity(community2, FEE, equalTo(2));

    // change a community of user
    changeCommunity(user1, asList(community1.getId(), community2.getId()));
    waitUntilTokenTransactionCompleted();
    checkBalanceOfUser(user1, community1, equalTo(899));
    checkBalanceOfUser(user1, community2, equalTo(798));

    // transfer token to user from admin (fee)
    transferTokenFromAdmin(com1Admin, user1, FEE, 0.1F);
    waitUntilTokenTransactionCompleted();
    checkBalanceOfUser(user1, community1, equalTo(899.1F));
    checkTokenBalanceOfCommunity(community1, FEE, equalTo(0.9F));
    
    // transfer ether to community
    checkEthBalanceOfCommunity(community1, lessThanOrEqualTo(1F));
    transferEtherToCommunity(community1, "1");
    waitUntilEthTransactionCompleted();
    checkEthBalanceOfCommunity(community1, greaterThan(1F));
    
    // redistribution
    createRedistribution(community1, true, null, "20");
    createRedistribution(community1, false, user1, "20");
    createRedistribution(community2, true, null, "30");
    createRedistribution(community2, false, user1, "30");
    redistribution();
    waitUntilEthTransactionCompleted();
    checkBalanceOfUser(user1, community1, equalTo(899.4F)); // +0.3
    checkBalanceOfUser(user1, community2, equalTo(798.9F)); // +0.9
    checkBalanceOfUser(user2, community1, equalTo(100.1F)); // +0.1
    checkBalanceOfUser(user2, community2, equalTo(200.3F)); // +0.3
    checkTokenBalanceOfCommunity(community1, FEE, equalTo(0.5F)); // -0.4
    checkTokenBalanceOfCommunity(community2, FEE, equalTo(0.8F)); // -1.2
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
      .multiPart("transactionFee", "1")
      .cookie("JSESSIONID", sessionId)
      .when().post("/admin/communities")
      .then().statusCode(200)
      .body("communityName", equalTo(communityName))
      .body("tokenName", equalTo(tokenName))
      .body("tokenSymbol", equalTo(tokenSymbol))
      .extract().path("communityId");
    
    Community community = emService.get().find(Community.class, (long) id);
    
    update(community.setPublishStatus(PUBLIC));
    log.info(String.format("creating community completed. [communityName=%s]", communityName));
    return community;
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

  private void createRedistribution(
      Community com,
      boolean isAll,
      User user,
      String redistributionRate
      ) throws Exception {
    log.info(String.format("creating redistribution started. [communityId=%d]", com.getId()));

    sessionId = loginAdmin(ncl.getEmailAddress(), "passpass");

    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", com.getId());
    requestParam.put("isAll", isAll);
    requestParam.put("redistributionRate", redistributionRate);
    if (user != null) {
      requestParam.put("userId", user.getId());
    }

    // create redistribution
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/communities/{id}/redistributions", com.getId())
      .then().statusCode(200);
    
    log.info(String.format("creating redistribution completed. [communityId=%d]", com.getId()));
  }

  private void redistribution() throws Exception {
    log.info(String.format("redistribution started."));

    // create redistribution
    given()
      .when().post("/batch/redistribution")
      .then().statusCode(200);
    
    log.info(String.format("redistribution completed."));
  }

  private void transferTokenFromAdmin(Admin admin, User to, WalletType walletType, double amount) {
    sessionId = loginAdmin(admin.getEmailAddress(), "passpass");

    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", admin.getCommunity().getId());
    requestParam.put("wallet", walletType.name());
    requestParam.put("beneficiaryUserId", to.getId());
    requestParam.put("amount", amount);

    // send token from main wallet
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/coin")
      .then().statusCode(200);
  }

  private void transferTokenFromUser(User from, User to, Community community, int amount, Matcher<?> expectAmount) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", to.getId());
    requestParam.put("description", "description");
    requestParam.put("transactionFee", 1);
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
  
  private void transferEtherToCommunity(Community community, String amount) throws Exception {
    log.info(String.format("transfer ether to main wallet started. [main wallet=%s]", community.getMainWalletAddress()));

    sessionId = loginAdmin(ncl.getEmailAddress(), "passpass");
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("beneficiaryCommunityId", community.getId());
    requestParam.put("amount", amount);

    // send ether to community
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/eth")
      .then().statusCode(200);
    
    log.info(String.format("transfer ether to main wallet completed. [username=%s]", community.getMainWalletAddress()));
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
  
  private void checkBalanceOfUser(User user, Community community, Matcher<?> expected) {
    sessionId = loginApp(user.getUsername(), "passpass");
    Object balance = given().cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/balance?communityId={communityId}", APP_API_VERSION.getMajor(), community.getId())
      .then().statusCode(200).body("balance", expected)
      .extract().path("balance");
    log.info(String.format("check balance ok. [user=%s, community=%s, balance=%s]", user.getUsername(), community.getName(), balance.toString()));
  }
  
  private void checkTokenBalanceOfCommunity(Community com, WalletType walletType, Matcher<?> expect) {
    sessionId = loginAdmin(ncl.getEmailAddress(), "passpass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin/balance?communityId={id}&wallet={wallet}", com.getId(), walletType.name())
      .then().statusCode(200)
      .body("balance", expect);
  }
  
  private void checkEthBalanceOfCommunity(Community com, Matcher<?> expect) {
    sessionId = loginAdmin(ncl.getEmailAddress(), "passpass");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance?communityId={id}", com.getId())
      .then().statusCode(200)
      .body("balance", expect);
  }

  private void waitUntilTokenTransactionCompleted() throws Exception {
    log.info(String.format("waiting for token transaction."));
    for (int i = 0; i < 60; i++) {
      long count = emService.get().createQuery(
          "SELECT count(t) FROM TokenTransaction t WHERE blockchainCompletedAt is null", Long.class)
          .getSingleResult();
      if(count == 0) {
        log.info(String.format("token transaction completed."));
        return;
      }
      Thread.sleep(1000);
    }
    throw new RuntimeException("token transaction didn't finish in 60 seconds.");
  }

  private void waitUntilEthTransactionCompleted() throws Exception {
    log.info(String.format("waiting for eth transaction."));
    for (int i = 0; i < 60; i++) {
      long count = emService.get().createQuery(
          "SELECT count(t) FROM EthTransaction t WHERE blockchainCompletedAt is null", Long.class)
          .getSingleResult();
      if(count == 0) {
        log.info(String.format("eth transaction completed."));
        return;
      }
      Thread.sleep(1000);
    }
    throw new RuntimeException("eth transaction didn't finish in 60 seconds.");
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
