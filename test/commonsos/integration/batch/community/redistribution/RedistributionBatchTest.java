package commonsos.integration.batch.community.redistribution;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.NCL;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;

public class RedistributionBatchTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private User user1;
  private User user2;
  private User user3;
  private Admin ncl;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setFee(BigDecimal.ONE).setPublishStatus(PUBLIC).setTokenContractAddress("0x0").setName("com1"));
    com2 =  create(new Community().setFee(BigDecimal.ONE).setPublishStatus(PUBLIC).setTokenContractAddress("0x0").setName("com2"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1), new CommunityUser().setCommunity(com2))));
    user3 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));

    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("pass")).setRole(NCL));
  }
  
  @Test
  public void redistribution_noTran() throws Exception {
    // redistribution
    given()
      .when().post("/batch/redistribution")
      .then().statusCode(200);
  }
  
  @Test
  public void redistribution_oneCommunityTran() throws Exception {
    // prepare
    create(new TokenTransaction().setCommunityId(com1.getId()).setFeeTransaction(true).setRedistributed(false).setAmount(new BigDecimal("100")));
    create(new TokenTransaction().setCommunityId(com1.getId()).setFeeTransaction(true).setRedistributed(false).setAmount(new BigDecimal("100")));
    create(new Redistribution().setCommunity(com1).setAll(true).setRate(new BigDecimal("1")));
    create(new Redistribution().setCommunity(com1).setUser(user1).setRate(new BigDecimal("1")));
    
    // redistribution
    given()
      .when().post("/batch/redistribution")
      .then().statusCode(200);
    
    // verify db
    Long count = emService.get().createQuery("SELECT count(*) FROM TokenTransaction WHERE isFeeTransaction is true AND redistributed is false", Long.class).getSingleResult();
    assertThat(count).isEqualTo(0L);

    List<TokenTransaction> tranList = emService.get().createQuery("FROM TokenTransaction WHERE isRedistributionTransaction is true", TokenTransaction.class).getResultList();
    tranList.sort((a,b) -> a.getBeneficiaryUserId().compareTo(b.getBeneficiaryUserId()));
    assertThat(tranList).extracting(TokenTransaction::getBeneficiaryUserId).containsExactly(user1.getId(), user2.getId(), user3.getId());
    assertThat(tranList).extracting(TokenTransaction::getAmount).containsExactly(new BigDecimal("2.66"), new BigDecimal("0.66"), new BigDecimal("0.66"));
  }
  
  @Test
  public void redistribution_twoCommunityTran() throws Exception {
    // prepare
    create(new TokenTransaction().setCommunityId(com1.getId()).setFeeTransaction(true).setRedistributed(false).setAmount(new BigDecimal("100")));
    create(new TokenTransaction().setCommunityId(com1.getId()).setFeeTransaction(true).setRedistributed(false).setAmount(new BigDecimal("100")));
    create(new Redistribution().setCommunity(com1).setAll(true).setRate(new BigDecimal("1")));
    create(new Redistribution().setCommunity(com1).setUser(user1).setRate(new BigDecimal("1")));

    create(new TokenTransaction().setCommunityId(com2.getId()).setFeeTransaction(true).setRedistributed(false).setAmount(new BigDecimal("0.5")));
    create(new TokenTransaction().setCommunityId(com2.getId()).setFeeTransaction(true).setRedistributed(false).setAmount(new BigDecimal("0.5")));
    create(new Redistribution().setCommunity(com2).setAll(true).setRate(new BigDecimal("1")));
    create(new Redistribution().setCommunity(com2).setUser(user1).setRate(new BigDecimal("1")));
    
    // redistribution
    given()
      .when().post("/batch/redistribution")
      .then().statusCode(200);
    
    // verify db
    Long count = emService.get().createQuery("SELECT count(*) FROM TokenTransaction WHERE isFeeTransaction is true AND redistributed is false", Long.class).getSingleResult();
    assertThat(count).isEqualTo(0L);

    List<TokenTransaction> tranList = emService.get().createQuery("FROM TokenTransaction WHERE isRedistributionTransaction is true", TokenTransaction.class).getResultList();
    tranList.sort((a,b) -> a.getBeneficiaryUserId().compareTo(b.getBeneficiaryUserId()));
    tranList.sort((a,b) -> a.getCommunityId().compareTo(b.getCommunityId()));
    assertThat(tranList).extracting(TokenTransaction::getCommunityId).containsExactly(com1.getId(), com1.getId(), com1.getId(), com2.getId());
    assertThat(tranList).extracting(TokenTransaction::getBeneficiaryUserId).containsExactly(user1.getId(), user2.getId(), user3.getId(), user1.getId());
    assertThat(tranList).extracting(TokenTransaction::getAmount).containsExactly(new BigDecimal("2.66"), new BigDecimal("0.66"), new BigDecimal("0.66"), new BigDecimal("0.01"));
  }
}
