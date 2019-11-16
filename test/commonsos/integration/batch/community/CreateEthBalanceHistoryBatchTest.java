package commonsos.integration.batch.community;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.EthBalanceHistory;

public class CreateEthBalanceHistoryBatchTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setFee(BigDecimal.ONE).setPublishStatus(PUBLIC).setTokenContractAddress("0x0").setName("com1"));
    com2 =  create(new Community().setFee(BigDecimal.ONE).setPublishStatus(PRIVATE).setTokenContractAddress("0x0").setName("com2"));
  }
  
  @Test
  public void createEthBalanceHistory() throws Exception {
    // execute
    given()
      .when().post("/batch/createEthBalanceHistory")
      .then().statusCode(200);
    
    // verify db
    List<EthBalanceHistory> resultList = emService.get().createQuery("FROM EthBalanceHistory ORDER BY id", EthBalanceHistory.class).getResultList();
    assertThat(resultList.size()).isEqualTo(2);
    assertThat(resultList.get(0).getCommunity()).isEqualTo(com1);
    assertThat(resultList.get(0).getBaseDate()).isEqualTo(LocalDate.now());
    assertThat(resultList.get(0).getEthBalance()).isNotNull();
    assertThat(resultList.get(1).getCommunity()).isEqualTo(com2);
    assertThat(resultList.get(1).getBaseDate()).isEqualTo(LocalDate.now());
    assertThat(resultList.get(1).getEthBalance()).isNotNull();
  }
}
