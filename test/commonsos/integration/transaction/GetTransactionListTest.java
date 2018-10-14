package commonsos.integration.transaction;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.community.Community;
import commonsos.repository.transaction.Transaction;
import commonsos.repository.user.User;

public class GetTransactionListTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User user1;
  private User user2;
  private User user3;
  private Transaction tran1;
  private Transaction tran2;
  private Transaction tran3;
  private Transaction tran4;
  private String sessionId;
  
  @Before
  public void setup() {
    community1 =  create(new Community().setName("community1"));
    community2 =  create(new Community().setName("community2"));
    user1 = create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityId(community1.getId()));
    user2 = create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityId(community1.getId()));
    user3 = create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityId(community2.getId()));
    Instant instant = Instant.now();
    tran1 = create(new Transaction().setRemitterId(user1.getId()).setBeneficiaryId(user2.getId()).setAmount(new BigDecimal(1)).setCreatedAt(instant.plusSeconds(10)));
    tran2 = create(new Transaction().setRemitterId(user1.getId()).setBeneficiaryId(user3.getId()).setAmount(new BigDecimal(2)).setCreatedAt(instant.plusSeconds(20)));
    tran3 = create(new Transaction().setRemitterId(user2.getId()).setBeneficiaryId(user1.getId()).setAmount(new BigDecimal(3)).setCreatedAt(instant.plusSeconds(30)));
    tran4 = create(new Transaction().setRemitterId(user3.getId()).setBeneficiaryId(user1.getId()).setAmount(new BigDecimal(4)).setCreatedAt(instant.plusSeconds(40)));
    

    sessionId = login("user1", "pass");
  }
  
  @Test
  public void transactionList() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/transactions?communityId={communityId}", community1.getId())
      .then().statusCode(200)
      .body("remitter.username",    contains("user3", "user2", "user1", "user1"))
      .body("beneficiary.username", contains("user1", "user1", "user3", "user2"));
  }
}
