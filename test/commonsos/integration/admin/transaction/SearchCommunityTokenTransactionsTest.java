package commonsos.integration.admin.transaction;

import static commonsos.repository.entity.CommunityStatus.PRIVATE;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;

public class SearchCommunityTokenTransactionsTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Community com3;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Admin com2Admin;
  private Admin com2Teller;
  private Admin nonComAdmin;
  private Admin nonComTeller;
  private User user1;
  private User user2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    com3 =  create(new Community().setName("com3").setStatus(PRIVATE));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com2Admin = create(new Admin().setEmailAddress("com2Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com2));
    com2Teller = create(new Admin().setEmailAddress("com2Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com2));

    nonComAdmin = create(new Admin().setEmailAddress("nonComAdmin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(null));
    nonComTeller = create(new Admin().setEmailAddress("nonComTeller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(null));

    user1 = create(new User().setUsername("user1").setEmailAddress("user1@a.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    user2 = create(new User().setUsername("user2").setEmailAddress("user2@b.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));

    create(new TokenTransaction().setCommunityId(com1.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setRemitterAdminId(ncl.getId()).setBeneficiaryUserId(user1.getId()).setAmount(new BigDecimal(10)));
    create(new TokenTransaction().setCommunityId(com1.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setRemitterAdminId(com1Admin.getId()).setBeneficiaryUserId(user2.getId()).setAmount(new BigDecimal(9)));
    create(new TokenTransaction().setCommunityId(com1.getId()).setFromAdmin(true).setWalletDivision(WalletType.FEE).setRemitterAdminId(com1Admin.getId()).setBeneficiaryUserId(user1.getId()).setAmount(new BigDecimal(8)));
    create(new TokenTransaction().setCommunityId(com1.getId()).setRemitterUserId(user1.getId()).setBeneficiaryUserId(user2.getId()).setAmount(new BigDecimal(1)));
    create(new TokenTransaction().setCommunityId(com2.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setRemitterAdminId(com2Admin.getId()).setBeneficiaryUserId(user1.getId()).setAmount(new BigDecimal(10)));
  }
  
  @Test
  public void getTran_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com1.getId(), "main")
      .then().statusCode(200)
      .body("transactionList.communityId",  contains(com1.getId().intValue(), com1.getId().intValue()))
      .body("transactionList.wallet",  contains("MAIN", "MAIN"))
      .body("transactionList.isFromAdmin",  contains(true, true))
      .body("transactionList.remitterAdmin.id",  contains(com1Admin.getId().intValue(), ncl.getId().intValue()))
      .body("transactionList.remitterAdmin.adminuser",  contains(com1Admin.getAdminname(), ncl.getAdminname()))
      .body("transactionList.remitter",  contains(nullValue(), nullValue()))
      .body("transactionList.beneficiary.id",  contains(user2.getId().intValue(), user1.getId().intValue()))
      .body("transactionList.beneficiary.username",  contains(user2.getUsername(), user1.getUsername()))
      .body("transactionList.amount",  contains(9F, 10F))
      .body("transactionList.debit",  contains(true, true));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com1.getId(), "fee")
      .then().statusCode(200)
      .body("transactionList.wallet",  contains("FEE"))
      .body("transactionList.remitterAdmin.id",  contains(com1Admin.getId().intValue()))
      .body("transactionList.remitter",  contains(nullValue()))
      .body("transactionList.beneficiary.id",  contains(user1.getId().intValue()));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com3.getId(), "main")
      .then().statusCode(200)
      .body("transactionList",  empty());

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?wallet={wallet}", "main")
      .then().statusCode(400);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}", com1.getId())
      .then().statusCode(400);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com1.getId(), "invalid")
      .then().statusCode(400);

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", -1L, "main")
      .then().statusCode(400);
  }
  
  @Test
  public void getTran_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com1.getId(), "main")
      .then().statusCode(200)
      .body("transactionList.amount",  contains(9F, 10F));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com2.getId(), "main")
      .then().statusCode(403);
  }
  
  @Test
  public void getTran_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com1.getId(), "main")
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com2.getId(), "main")
      .then().statusCode(403);
  }
  
  @Test
  public void getTran_byNonComAdmin() {
    sessionId = loginAdmin(nonComAdmin.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com1.getId(), "main")
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com2.getId(), "main")
      .then().statusCode(403);
  }
  
  @Test
  public void getTran_byNonComTeller() {
    sessionId = loginAdmin(nonComTeller.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com1.getId(), "main")
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}", com2.getId(), "main")
      .then().statusCode(403);
  }

  @Test
  public void searchCommunity_pagination() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    Community page_com = create(new Community().setName("page_com"));
    User page_user = create(new User().setUsername("page_user").setEmailAddress("page_user@a.com").setCommunityUserList(asList(new CommunityUser().setCommunity(page_com))));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(1)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(2)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(3)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(4)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(5)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(6)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(7)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(8)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(9)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(10)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(11)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryUserId(page_user.getId()).setAmount(new BigDecimal(12)));

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "MAIN", "0", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.amount", contains(
          12F, 11F, 10F, 9F, 8F, 7F, 6F, 5F, 4F, 3F))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "MAIN", "1", "10", "ASC")
      .then().statusCode(200)
      .body("transactionList.amount", contains(
          2F, 1F))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "MAIN", "0", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.amount", contains(
          1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F, 10F))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/coin?communityId={comId}&wallet={wallet}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "MAIN", "1", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.amount", contains(
          11F, 12F))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
