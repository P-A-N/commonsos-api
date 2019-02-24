package commonsos.integration.community;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;

public class GetSearchCommutityTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  
  @Before
  public void createUser() {
    community1 = create(new Community().setName("comm_foo").setTokenContractAddress("0x0"));
    community2 = create(new Community().setName("comm_foo_bar").setTokenContractAddress("0x0"));
    community3 = create(new Community().setName("comm_bar").setTokenContractAddress("0x0"));
  }
  
  @Test
  public void searchCommutity() throws Exception {
    // non filter
    given()
      .when().get("/communities")
      .then().statusCode(200)
      .body("id", iterableWithSize(3))
      .body("id", contains(community1.getId().intValue(), community2.getId().intValue(), community3.getId().intValue()));

    // filter
    String body = given()
      .when().get("/communities?filter={filter}", "foo")
      .then().statusCode(200)
      .body("id", iterableWithSize(2))
      .body("id", contains(community1.getId().intValue(), community2.getId().intValue()))
      .extract().asString();

    assertThat(body).doesNotContain("walletLastViewTime", "adLastViewTime", "notificationLastViewTime");
  }
}
