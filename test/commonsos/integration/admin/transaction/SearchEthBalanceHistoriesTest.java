package commonsos.integration.admin.transaction;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.time.LocalDate.parse;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.EthBalanceHistory;

public class SearchEthBalanceHistoriesTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setPublishStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setPublishStatus(PRIVATE));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-01")).setEthBalance(new BigDecimal("100")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-02")).setEthBalance(new BigDecimal("99")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-03")).setEthBalance(new BigDecimal("98")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-04")).setEthBalance(new BigDecimal("97")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-05")).setEthBalance(new BigDecimal("96")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-06")).setEthBalance(new BigDecimal("95")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-07")).setEthBalance(new BigDecimal("94")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-08")).setEthBalance(new BigDecimal("93")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-09")).setEthBalance(new BigDecimal("92")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-10")).setEthBalance(new BigDecimal("91")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-11")).setEthBalance(new BigDecimal("100")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-12")).setEthBalance(new BigDecimal("99")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-13")).setEthBalance(new BigDecimal("98")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-14")).setEthBalance(new BigDecimal("97")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-15")).setEthBalance(new BigDecimal("96")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-16")).setEthBalance(new BigDecimal("95")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-17")).setEthBalance(new BigDecimal("94")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-18")).setEthBalance(new BigDecimal("93")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-19")).setEthBalance(new BigDecimal("92")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-20")).setEthBalance(new BigDecimal("91")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-21")).setEthBalance(new BigDecimal("100")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-22")).setEthBalance(new BigDecimal("99")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-23")).setEthBalance(new BigDecimal("98")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-24")).setEthBalance(new BigDecimal("97")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-25")).setEthBalance(new BigDecimal("96")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-26")).setEthBalance(new BigDecimal("95")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-27")).setEthBalance(new BigDecimal("94")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-28")).setEthBalance(new BigDecimal("93")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-29")).setEthBalance(new BigDecimal("92")));
    create(new EthBalanceHistory().setCommunity(com1).setBaseDate(parse("2019-11-30")).setEthBalance(new BigDecimal("91")));
  }
  
  @Test
  public void searchEthBalanceHistories_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}", com1.getId())
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-01", "2019-11-02", "2019-11-03", "2019-11-04", "2019-11-05", "2019-11-06", "2019-11-07", "2019-11-08", "2019-11-09", "2019-11-10",
          "2019-11-11", "2019-11-12", "2019-11-13", "2019-11-14", "2019-11-15", "2019-11-16", "2019-11-17", "2019-11-18", "2019-11-19", "2019-11-20",
          "2019-11-21", "2019-11-22", "2019-11-23", "2019-11-24", "2019-11-25", "2019-11-26", "2019-11-27", "2019-11-28", "2019-11-29", "2019-11-30"))
      .body("balanceList.balance", contains(
          100f, 99f, 98f, 97f, 96f, 95f, 94f, 93f, 92f, 91f,
          100f, 99f, 98f, 97f, 96f, 95f, 94f, 93f, 92f, 91f,
          100f, 99f, 98f, 97f, 96f, 95f, 94f, 93f, 92f, 91f));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}&from={from}", com1.getId(), "2019-11-21")
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-21", "2019-11-22", "2019-11-23", "2019-11-24", "2019-11-25", "2019-11-26", "2019-11-27", "2019-11-28", "2019-11-29", "2019-11-30"))
      .body("balanceList.balance", contains(
          100f, 99f, 98f, 97f, 96f, 95f, 94f, 93f, 92f, 91f));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}&to={to}", com1.getId(), "2019-11-10")
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-01", "2019-11-02", "2019-11-03", "2019-11-04", "2019-11-05", "2019-11-06", "2019-11-07", "2019-11-08", "2019-11-09", "2019-11-10"))
      .body("balanceList.balance", contains(
          100f, 99f, 98f, 97f, 96f, 95f, 94f, 93f, 92f, 91f));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}&from={from}&to={to}", com1.getId(), "2019-11-11", "2019-11-20")
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-11", "2019-11-12", "2019-11-13", "2019-11-14", "2019-11-15", "2019-11-16", "2019-11-17", "2019-11-18", "2019-11-19", "2019-11-20"))
      .body("balanceList.balance", contains(
          100f, 99f, 98f, 97f, 96f, 95f, 94f, 93f, 92f, 91f));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}", com2.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void getTran_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}", com1.getId())
      .then().statusCode(200);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getTran_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}", com1.getId())
      .then().statusCode(403);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}", com2.getId())
      .then().statusCode(403);
  }

  @Test
  public void searchCommunity_pagination() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com1.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-01", "2019-11-02", "2019-11-03", "2019-11-04", "2019-11-05", "2019-11-06", "2019-11-07", "2019-11-08", "2019-11-09", "2019-11-10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(2));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com1.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-11", "2019-11-12", "2019-11-13", "2019-11-14", "2019-11-15", "2019-11-16", "2019-11-17", "2019-11-18", "2019-11-19", "2019-11-20"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(2));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com1.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-30", "2019-11-29", "2019-11-28", "2019-11-27", "2019-11-26", "2019-11-25", "2019-11-24", "2019-11-23", "2019-11-22", "2019-11-21"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(2));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance/histories?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", com1.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("balanceList.baseDate", contains(
          "2019-11-20", "2019-11-19", "2019-11-18", "2019-11-17", "2019-11-16", "2019-11-15", "2019-11-14", "2019-11-13", "2019-11-12", "2019-11-11"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(2));
  }
}
