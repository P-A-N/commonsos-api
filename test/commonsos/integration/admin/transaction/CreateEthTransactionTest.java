package commonsos.integration.admin.transaction;

import static commonsos.repository.entity.CommunityStatus.PRIVATE;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.EthTransaction;

public class CreateEthTransactionTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private String sessionId;

  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC).setFee(ONE));
    com2 =  create(new Community().setName("com2").setStatus(PRIVATE).setFee(ONE));

    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }

  @Test
  public void createTransaction_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    // send ether
    Map<String, Object> requestParam = getRequestParam(com1, "10");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/eth")
      .then().statusCode(200);

    // verify db transaction
    EthTransaction transaction = emService.get().createQuery("FROM EthTransaction ORDER BY id DESC", EthTransaction.class).setMaxResults(1).getSingleResult();
    assertThat(transaction.getCommunityId()).isEqualTo(com1.getId());
    assertThat(transaction.getRemitterAdminId()).isEqualTo(ncl.getId());
    assertThat(transaction.getAmount()).isEqualByComparingTo(TEN);

    // send ether
    requestParam = getRequestParam(com1, "9.99");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/eth")
      .then().statusCode(200);

    // verify db transaction
    transaction = emService.get().createQuery("FROM EthTransaction ORDER BY id DESC", EthTransaction.class).setMaxResults(1).getSingleResult();
    assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("9.99"));
    
    // send ether (not enough ether)
    requestParam = getRequestParam(com1, "10.1");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/eth")
      .then().statusCode(468);

  }

  @Test
  public void createTransaction_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    // send ether
    Map<String, Object> requestParam = getRequestParam(com1, "10");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/eth")
      .then().statusCode(403);
  }

  @Test
  public void createTransaction_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    // send ether
    Map<String, Object> requestParam = getRequestParam(com1, "10");
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/admin/transactions/eth")
      .then().statusCode(403);
  }

  private Map<String, Object> getRequestParam(Community beneficiaryCommunity, String amount) {
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("beneficiaryCommunityId", beneficiaryCommunity.getId());
    requestParam.put("amount", amount);
    return requestParam;
  }
}
