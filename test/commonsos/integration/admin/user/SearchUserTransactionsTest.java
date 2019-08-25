package commonsos.integration.admin.user;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

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

public class SearchUserTransactionsTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private Admin com2Admin;
  private Admin com2Teller;
  private Admin nonComAdmin;
  private Admin nonComTeller;
  private User com1com2User;
  private User com1User1;
  private User com1User2;
  private User com2User1;
  private User nonComUser;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    com2Admin = create(new Admin().setEmailAddress("com2Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com2));
    com2Teller = create(new Admin().setEmailAddress("com2Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com2));

    nonComAdmin = create(new Admin().setEmailAddress("nonComAdmin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(null));
    nonComTeller = create(new Admin().setEmailAddress("nonComTeller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(null));

    com1com2User = create(new User().setUsername("com1com2User").setEmailAddress("com1com2User@d.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    com1User1 = create(new User().setUsername("com1User1").setEmailAddress("com1User1@a.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com1User2 = create(new User().setUsername("com1User2").setEmailAddress("com1User2@b.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    com2User1 = create(new User().setUsername("com2User1").setEmailAddress("com2User1@c.com").setCommunityUserList(asList(new CommunityUser().setCommunity(com2))));
    nonComUser = create(new User().setUsername("nonComUser").setEmailAddress("nonComUser@e.com").setCommunityUserList(asList()));

    create(new TokenTransaction().setCommunityId(com1.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(com1com2User.getId()).setAmount(new BigDecimal(10)));
    create(new TokenTransaction().setCommunityId(com1.getId()).setFromAdmin(true).setWalletDivision(WalletType.FEE).setBeneficiaryId(com1com2User.getId()).setAmount(new BigDecimal(9)));
    create(new TokenTransaction().setCommunityId(com1.getId()).setRemitterId(com1com2User.getId()).setBeneficiaryId(com1User1.getId()).setAmount(new BigDecimal(1)));
    create(new TokenTransaction().setCommunityId(com1.getId()).setRemitterId(com1User1.getId()).setBeneficiaryId(com1com2User.getId()).setAmount(new BigDecimal(2)));
    create(new TokenTransaction().setCommunityId(com2.getId()).setRemitterId(com1com2User.getId()).setBeneficiaryId(com2User1.getId()).setAmount(new BigDecimal(3)));
    create(new TokenTransaction().setCommunityId(com2.getId()).setRemitterId(com2User1.getId()).setBeneficiaryId(com1com2User.getId()).setAmount(new BigDecimal(4)));
  }
  
  @Test
  public void getUser_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com1.getId())
      .then().statusCode(200)
      .body("transactionList.communityId",  contains(com1.getId().intValue(), com1.getId().intValue(), com1.getId().intValue(), com1.getId().intValue()))
      .body("transactionList.wallet",  contains(null, null, "FEE", "MAIN"))
      .body("transactionList.isFromAdmin",  contains(false, false, true, true))
      .body("transactionList.remitter.id",  contains(com1User1.getId().intValue(), com1com2User.getId().intValue()))
      .body("transactionList.remitter.username",  contains(com1User1.getUsername(), com1com2User.getUsername()))
      .body("transactionList.beneficiary.id",  contains(com1com2User.getId().intValue(), com1User1.getId().intValue(), com1com2User.getId().intValue(), com1com2User.getId().intValue()))
      .body("transactionList.beneficiary.username",  contains(com1com2User.getUsername(), com1User1.getUsername(), com1com2User.getUsername(), com1com2User.getUsername()))
      .body("transactionList.amount",  contains(2F, 1F, 9F, 10F))
      .body("transactionList.debit",  contains(false, false, true, true));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1User1.getId(), com1.getId())
      .then().statusCode(200)
      .body("transactionList.amount",  contains(2F, 1F));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1User2.getId(), com1.getId())
      .then().statusCode(200)
      .body("transactionList.amount",  empty());

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com2.getId())
      .then().statusCode(200)
      .body("transactionList.amount",  contains(4F, 3F));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com2User1.getId(), com2.getId())
      .then().statusCode(200)
      .body("transactionList.amount",  contains(4F, 3F));

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", nonComUser.getId(), com2.getId())
      .then().statusCode(200)
      .body("transactionList.amount",  empty());
  }
  
  @Test
  public void getUser_byCom1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com1.getId())
      .then().statusCode(200)
      .body("transactionList.amount",  contains(2F, 1F, 9F, 10F));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byCom1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com1.getId())
      .then().statusCode(200)
      .body("transactionList.amount",  contains(2F, 1F, 9F, 10F));
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byNonComAdmin() {
    sessionId = loginAdmin(nonComAdmin.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com1.getId())
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void getUser_byNonComTeller() {
    sessionId = loginAdmin(nonComTeller.getEmailAddress(), "password");
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com1.getId())
      .then().statusCode(403);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}", com1com2User.getId(), com2.getId())
      .then().statusCode(403);
  }

  @Test
  public void searchCommunity_pagination() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    Community page_com = create(new Community().setName("page_com"));
    User page_user = create(new User().setUsername("page_user").setEmailAddress("page_user@a.com").setCommunityUserList(asList(new CommunityUser().setCommunity(page_com))));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(1)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(2)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(3)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(4)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(5)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(6)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(7)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(8)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(9)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(10)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(11)));
    create(new TokenTransaction().setCommunityId(page_com.getId()).setFromAdmin(true).setWalletDivision(WalletType.MAIN).setBeneficiaryId(page_user.getId()).setAmount(new BigDecimal(12)));

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/users/{id}/transactions?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_user.getId(), page_com.getId(), "0", "10", "ASC")
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
      .when().get("/admin/users/{id}/transactions?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_user.getId(), page_com.getId(), "1", "10", "ASC")
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
      .when().get("/admin/users/{id}/transactions?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_user.getId(), page_com.getId(), "0", "10", "DESC")
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
      .when().get("/admin/users/{id}/transactions?communityId={comId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_user.getId(), page_com.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("transactionList.amount", contains(
          11F, 12F))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
