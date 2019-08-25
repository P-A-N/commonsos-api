package commonsos.integration.app.community;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;

public class GetSearchCommutityTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  
  @BeforeEach
  public void createUser() throws Exception {
    community1 = create(new Community().setStatus(PUBLIC).setName("comm_foo").setTokenContractAddress("0x0"));
    community2 = create(new Community().setStatus(PUBLIC).setName("comm_foo_bar").setTokenContractAddress("0x0"));
    community3 = create(new Community().setStatus(PUBLIC).setName("comm_bar").setTokenContractAddress("0x0"));
  }
  
  @Test
  public void searchCommutity() throws Exception {
    // non filter
    given()
      .when().get("/communities")
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(3))
      .body("communityList.id", contains(community1.getId().intValue(), community2.getId().intValue(), community3.getId().intValue()));

    // filter
    String body = given()
      .when().get("/communities?filter={filter}", "foo")
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(2))
      .body("communityList.id", contains(community1.getId().intValue(), community2.getId().intValue()))
      .extract().asString();

    assertThat(body).doesNotContain("walletLastViewTime", "adLastViewTime", "notificationLastViewTime");
  }
  
  @Test
  public void searchCommutity_pagination() throws Exception {
    // prepare
    create(new Community().setStatus(PUBLIC).setName("page_community1").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community2").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community3").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community4").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community5").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community6").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community7").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community8").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community9").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community10").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community11").setTokenContractAddress("0x0"));
    create(new Community().setStatus(PUBLIC).setName("page_community12").setTokenContractAddress("0x0"));

    // page 0 size 10 asc
    // filter
    given()
      .when().get("/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "page", "0", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community1", "page_community2", "page_community3", "page_community4", "page_community5",
          "page_community6", "page_community7", "page_community8", "page_community9", "page_community10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    // filter
    given()
      .when().get("/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "page", "1", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community11", "page_community12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    // filter
    given()
      .when().get("/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "page", "0", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community12", "page_community11", "page_community10", "page_community9", "page_community8",
          "page_community7", "page_community6", "page_community5", "page_community4", "page_community3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    // filter
    given()
      .when().get("/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", "page", "1", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community2", "page_community1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
