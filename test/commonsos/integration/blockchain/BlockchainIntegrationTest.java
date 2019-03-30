package commonsos.integration.blockchain;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.WiserMessage;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class BlockchainIntegrationTest extends IntegrationTest {
  
  private Logger log = Logger.getLogger(this.getClass().getName());

  private Community community1;
  private Community community2;
  private User admin1;
  private User admin2;
  private User user1;
  private User user2;
  private String sessionId;

  @Override
  protected boolean isBlockchainEnable() { return true; }
  
  @BeforeAll
  public void setupTest() throws Exception {
    admin1 = createAdmin("admin1_test", "passpass", "admin1@test.com", true, null);
    admin2 = createAdmin("admin2_test", "passpass", "admin2@test.com", true, null);
    community1 = createCommunity(admin1, "community1", "c1", "community1");
    community2 = createCommunity(admin2, "community2", "c2", "community2");
  }
  
  @Test
  public void testBlockchain() throws Exception {
    // create user
    user1 = createUser("user1_test", "passpass", "user1@test.com", true, asList(community1.getId()));
    user2 = createUser("user2_test", "passpass", "user2@test.com", true, asList(community1.getId(), community2.getId()));
    checkBalance(user1, community1, 0);
    checkBalance(user2, community1, 0);
    checkBalance(user2, community2, 0);

    // transfer token from admin
    transferToken(admin1, user1, community1, 100);
    waitUntilTransactionCompleted();
    checkBalance(user1, community1, 100);

    // transfer token from user
    transferToken(user1, user2, community1, 10);
    waitUntilTransactionCompleted();
    checkBalance(user1, community1, 90);
    checkBalance(user2, community1, 10);
    
    // change community
    changeCommunity(user1, asList(community2.getId()));
    waitUntilAllowed(user1, community2);
    
    // transfer token from admin
    transferToken(admin2, user1, community2, 100);
    waitUntilTransactionCompleted();
    checkBalance(user1, community2, 100);

    // transfer token from user
    transferToken(user1, user2, community2, 20);
    waitUntilTransactionCompleted();
    checkBalance(user1, community2, 80);
    checkBalance(user2, community2, 20);

    // change community
    changeCommunity(user1, asList(community1.getId(), community2.getId()));
    waitUntilTransactionCompleted();
    checkBalance(user1, community1, 90);
    checkBalance(user1, community2, 80);
  }
  
  private User createAdmin(
      String username,
      String password,
      String emailAddress,
      boolean waitUntilCompleated,
      List<Long> communityList
      ) throws Exception {
    User admin = createUser(username, password, emailAddress, waitUntilCompleated, communityList);

    log.info(String.format("transfer ether to admin started. [username=%s]", username));
    transferEtherToAdmin(admin);
    log.info(String.format("transfer ether to admin completed. [username=%s]", username));
    
    return admin;
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
      .when().post("/create-account")
      .then().statusCode(200);
    
    // get accessId
    List<WiserMessage> messages = wiser.getMessages();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0).getEnvelopeReceiver()).isEqualTo(requestParam.get("emailAddress"));
    String accessId = extractAccessId(messages.get(0));
    messages.clear();
    
    // complete create user
    int userId = given()
      .when().post("/create-account/{accessId}", accessId)
      .then().statusCode(200)
      .extract().path("id");

    log.info(String.format("creating user completed. [username=%s]", username));
    
    User user = emService.get().find(User.class, (long) userId);
    return user;
  }
  
  private void transferEtherToAdmin(User admin) throws Exception {
    URL url = this.getClass().getResource("/blockchain/UTC--2018-11-08T09-10-38.833302600Z--766a1c4737970feddde6f2b8659fca05bd0339ab");
    Credentials commonsos = WalletUtils.loadCredentials("pass1", new File(url.toURI()));
    
    BigInteger initialEtherAmountForAdmin = BigInteger.TEN.pow(18);
    blockchainService.transferEther(commonsos, admin.getWalletAddress(), initialEtherAmountForAdmin);
  }
  
  private Community createCommunity(
      User admin,
      String communityName,
      String tokenSymbol,
      String tokenName) {
    log.info(String.format("creating community started. [communityName=%s]", communityName));
    
    String tokenAddress = blockchainService.createToken(admin, tokenSymbol, tokenName);
    Community community = create(new Community().setName(communityName).setTokenContractAddress(tokenAddress).setAdminUser(admin));

    update(admin.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));

    log.info(String.format("creating community completed. [communityName=%s]", communityName));
    return community;
  }
  
  private void transferToken(User from, User to, Community community, int amount) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("beneficiaryId", to.getId());
    requestParam.put("description", "description");
    requestParam.put("amount", amount);
    requestParam.put("adId", null);
    log.info(String.format("transfer token started. [from=%s, to=%s, community=%s]", community.getName(), from.getUsername(), to.getUsername()));
    
    sessionId = login(from.getUsername(), "passpass");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/transactions")
      .then().statusCode(200);
    log.info(String.format("transfer token completed. [from=%s, to=%s, community=%s]", community.getName(), from.getUsername(), to.getUsername()));
  }
  
  private void checkBalance(User user, Community community, int expected) {
    sessionId = login(user.getUsername(), "passpass");
    given().cookie("JSESSIONID", sessionId)
      .when().get("/balance?communityId={communityId}", community.getId())
      .then().statusCode(200).body("balance", equalTo(expected));
    log.info(String.format("check balance ok. [user=%s, community=%s, balance=%d]", user.getUsername(), community.getName(), expected));
  }
  
  private void changeCommunity(User user, List<Long> communityList) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityList", communityList);
    log.info(String.format("change community started. [user=%s]", user.getUsername()));
    
    sessionId = login(user.getUsername(), "passpass");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/communities", user.getId())
      .then().statusCode(200);
    log.info(String.format("change community completed. [user=%s]", user.getUsername()));
  }
  
  private void waitUntilTransactionCompleted() throws Exception {
    log.info(String.format("waiting for transaction."));
    for (int i = 0; i < 60; i++) {
      long count = emService.get().createQuery(
          "SELECT count(t) FROM Transaction t WHERE blockchainCompletedAt is null", Long.class)
          .getSingleResult();
      if(count == 0) {
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
