package commonsos.integration.app.transaction;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Transaction;
import commonsos.repository.entity.User;

public class GetTransactionListTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User admin1;
  private User admin2;
  
  private User user1;
  private User user2;
  private User user3;
//  private Transaction tran1;
//  private Transaction tran2;
//  private Transaction tran3;
//  private Transaction tran4;
  private String sessionId;
  
  @BeforeEach
  public void setup() {
    community1 =  create(new Community().setName("community1"));
    community2 =  create(new Community().setName("community2"));
    admin1 = create(new User().setUsername("admin1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    admin2 = create(new User().setUsername("admin2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community2))));
    update(community1.setAdminUser(admin1));
    update(community2.setAdminUser(admin2));
    
    user1 = create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1), new CommunityUser().setCommunity(community2))));
    user2 = create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    user3 = create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community2))));
    Instant instant = Instant.now();
    /* tran1 = */create(new Transaction().setCommunityId(community1.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setAmount(new BigDecimal(1)).setCreatedAt(instant.plusSeconds(10)));
    /* tran2 = */create(new Transaction().setCommunityId(community2.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user3.getId()).setAmount(new BigDecimal(2)).setCreatedAt(instant.plusSeconds(20)));
    /* tran3 = */create(new Transaction().setCommunityId(community1.getId()).setRemitterId(user2.getId()).setBeneficiaryId(user1.getId()).setAmount(new BigDecimal(3)).setCreatedAt(instant.plusSeconds(30)));
    /* tran4 = */create(new Transaction().setCommunityId(community2.getId()).setRemitterId(user3.getId()).setBeneficiaryId(user1.getId()).setAmount(new BigDecimal(4)).setCreatedAt(instant.plusSeconds(40)));
  }
  
  @Test
  public void transactionList() {
    sessionId = login("user1", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}", community1.getId())
      .then().statusCode(200)
      .body("transactionList.remitter.username",    contains("user2", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user2"));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}", community2.getId())
      .then().statusCode(200)
      .body("transactionList.remitter.username",    contains("user3", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user3"));
    
    sessionId = login("user2", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}", community1.getId())
      .then().statusCode(200)
      .body("transactionList.remitter.username",    contains("user2", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user2"));
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}", community2.getId())
      .then().statusCode(200)
      .body("transactionList.remitter.username",    empty())
      .body("transactionList.beneficiary.username", empty());
  }

  @Test
  public void transactionListByAdmin() {
    sessionId = login("admin1", "pass");

    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions?communityId={communityId}&userId={userId}", community1.getId(), user1.getId())
      .then().statusCode(200)
      .body("transactionList.remitter.username",    contains("user2", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user2"));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions?communityId={communityId}&userId={userId}", community2.getId(), user1.getId())
      .then().statusCode(403);

    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/transactions?communityId={communityId}&userId={userId}", community1.getId(), user3.getId())
    .then().statusCode(200)
    .body("transactionList.remitter.username",    empty())
    .body("transactionList.beneficiary.username", empty());

    sessionId = login("user1", "pass");

    // call by user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions?communityId={communityId}&userId={userId}", community1.getId(), user2.getId())
      .then().statusCode(403);
  }
  
  
  @Test
  public void transactionList_pagination() {
    // prepare
    Community community =  create(new Community().setName("page_community"));
    update(user1.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    update(user2.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("1").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1000)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("2").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1100)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("3").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1200)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("4").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1300)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("5").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1400)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("6").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1500)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("7").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(900)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("8").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(800)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("9").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(700)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("10").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(600)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("11").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(500)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("12").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(400)));

    sessionId = login("user1", "pass");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "12", "11", "10", "9", "8", "7", "1", "2", "3", "4"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "5", "6"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "6", "5", "4", "3", "2", "1", "7", "8", "9", "10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "11", "12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }

  @Test
  public void transactionListByAdmin_pagination() {
    // prepare
    Community community =  create(new Community().setName("page_community"));
    User admin = create(new User().setUsername("admin").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    update(community.setAdminUser(admin));

    update(user1.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    update(user2.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("1").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1000)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("2").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1100)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("3").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1200)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("4").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1300)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("5").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1400)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("6").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(1500)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("7").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(900)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("8").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(800)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("9").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(700)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("10").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(600)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("11").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(500)));
    create(new Transaction().setCommunityId(community.getId()).setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setDescription("12").setAmount(new BigDecimal("1")).setCreatedAt(Instant.now().minusSeconds(400)));

    sessionId = login("admin", "pass");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), user1.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "12", "11", "10", "9", "8", "7", "1", "2", "3", "4"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), user1.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "5", "6"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), user1.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "6", "5", "4", "3", "2", "1", "7", "8", "9", "10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", community.getId(), user1.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "11", "12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
