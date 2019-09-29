package commonsos.integration.app.transaction;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.TokenTransaction;
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
  public void setup() throws Exception {
    community1 =  create(new Community().setStatus(PUBLIC).setName("community1"));
    community2 =  create(new Community().setStatus(PUBLIC).setName("community2"));
    admin1 = create(new User().setUsername("admin1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    admin2 = create(new User().setUsername("admin2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community2))));
    update(community1.setAdminUser(admin1));
    update(community2.setAdminUser(admin2));
    
    user1 = create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1), new CommunityUser().setCommunity(community2))));
    user2 = create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community1))));
    user3 = create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community2))));
    create(new TokenTransaction().setCommunityId(community1.getId()).setFromAdmin(true).setBeneficiaryUserId(user1.getId()).setAmount(new BigDecimal(1)));
    create(new TokenTransaction().setCommunityId(community1.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setAmount(new BigDecimal(1)));
    create(new TokenTransaction().setCommunityId(community2.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user3.getId()).setAmount(new BigDecimal(2)));
    create(new TokenTransaction().setCommunityId(community1.getId()).setRemitterUserId(user2.getId()).setBeneficiaryUserId(user1.getId()).setAmount(new BigDecimal(3)));
    create(new TokenTransaction().setCommunityId(community2.getId()).setRemitterUserId(user3.getId()).setBeneficiaryUserId(user1.getId()).setAmount(new BigDecimal(4)));
  }
  
  @Test
  public void transactionList() {
    sessionId = loginApp("user1", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}", APP_API_VERSION.getMajor(), community1.getId())
      .then().statusCode(200)
      .body("transactionList.isFromAdmin",    contains(false, false, true))
      .body("transactionList.remitter.username",    contains("user2", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user2", "user1"));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}", APP_API_VERSION.getMajor(), community2.getId())
      .then().statusCode(200)
      .body("transactionList.isFromAdmin",    contains(false, false))
      .body("transactionList.remitter.username",    contains("user3", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user3"));
    
    sessionId = loginApp("user2", "pass");
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}", APP_API_VERSION.getMajor(), community1.getId())
      .then().statusCode(200)
      .body("transactionList.isFromAdmin",    contains(false, false))
      .body("transactionList.remitter.username",    contains("user2", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user2"));
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}", APP_API_VERSION.getMajor(), community2.getId())
      .then().statusCode(200)
      .body("transactionList.isFromAdmin",    empty())
      .body("transactionList.remitter.username",    empty())
      .body("transactionList.beneficiary.username", empty());
  }

  @Test
  public void transactionListByAdmin() {
    sessionId = loginApp("admin1", "pass");

    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}", APP_API_VERSION.getMajor(), community1.getId(), user1.getId())
      .then().statusCode(200)
      .body("transactionList.isFromAdmin",    contains(false, false, true))
      .body("transactionList.remitter.username",    contains("user2", "user1"))
      .body("transactionList.beneficiary.username", contains("user1", "user2", "user1"));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}", APP_API_VERSION.getMajor(), community2.getId(), user1.getId())
      .then().statusCode(403);

    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}", APP_API_VERSION.getMajor(), community1.getId(), user3.getId())
    .then().statusCode(200)
    .body("transactionList.isFromAdmin",    empty())
    .body("transactionList.remitter.username",    empty())
    .body("transactionList.beneficiary.username", empty());

    sessionId = loginApp("user1", "pass");

    // call by user
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}", APP_API_VERSION.getMajor(), community1.getId(), user2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void transactionList_pagination() throws Exception {
    // prepare
    Community community =  create(new Community().setStatus(PUBLIC).setName("page_community"));
    update(user1.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    update(user2.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("1").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("2").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("3").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("4").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("5").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("6").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("7").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("8").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("9").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("10").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("11").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("12").setAmount(new BigDecimal("1")));

    sessionId = loginApp("user1", "pass");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "12", "11", "10", "9", "8", "7", "6", "5", "4", "3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "2", "1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/transactions?communityId={communityId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "11", "12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }

  @Test
  public void transactionListByAdmin_pagination() throws Exception {
    // prepare
    Community community =  create(new Community().setStatus(PUBLIC).setName("page_community"));
    User admin = create(new User().setUsername("admin").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    update(community.setAdminUser(admin));

    update(user1.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    update(user2.setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("1").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("2").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("3").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("4").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("5").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("6").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("7").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("8").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("9").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("10").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("11").setAmount(new BigDecimal("1")));
    create(new TokenTransaction().setCommunityId(community.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setDescription("12").setAmount(new BigDecimal("1")));

    sessionId = loginApp("admin", "pass");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), user1.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "12", "11", "10", "9", "8", "7", "6", "5", "4", "3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), user1.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "2", "1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), user1.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/admin/transactions?communityId={communityId}&userId={userId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), community.getId(), user1.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.description", contains(
          "11", "12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
