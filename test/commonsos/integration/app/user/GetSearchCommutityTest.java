package commonsos.integration.app.user;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetSearchCommutityTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private Community community3;
  private Community community4;
  private User user;
  private String sessionId;
  
  @BeforeEach
  public void createUser() {
    community1 = create(new Community().setStatus(PUBLIC).setName("comm_foo").setTokenContractAddress("0x0"));
    community2 = create(new Community().setStatus(PUBLIC).setName("comm_foo_bar").setTokenContractAddress("0x0"));
    community3 = create(new Community().setStatus(PUBLIC).setName("comm_bar").setTokenContractAddress("0x0"));
    community4 = create(new Community().setStatus(PUBLIC).setName("comm_bar_foo").setTokenContractAddress("0x0"));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4))));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void searchCommutity() throws Exception {
    // non filter
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(3))
      .body("communityList.id", contains(community2.getId().intValue(), community3.getId().intValue(), community4.getId().intValue()))
      .body("communityList.balance", contains(10, 10, 10))
      .body("communityList.walletLastViewTime", contains(Instant.EPOCH.toString(), Instant.EPOCH.toString(), Instant.EPOCH.toString()));

    // filter
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?filter={filter}", user.getId(), "foo")
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(2))
      .body("communityList.id", contains(community2.getId().intValue(), community4.getId().intValue()))
      .body("communityList.walletLastViewTime", contains(Instant.EPOCH.toString(), Instant.EPOCH.toString()));
  }
  
  @Test
  public void searchCommutity_nonFilter_pagination() throws Exception {
    // prepare
    Community pageCommunity1 =  create(new Community().setStatus(PUBLIC).setName("page_community1").setTokenContractAddress("0x0"));
    Community pageCommunity2 =  create(new Community().setStatus(PUBLIC).setName("page_community2").setTokenContractAddress("0x0"));
    Community pageCommunity3 =  create(new Community().setStatus(PUBLIC).setName("page_community3").setTokenContractAddress("0x0"));
    Community pageCommunity4 =  create(new Community().setStatus(PUBLIC).setName("page_community4").setTokenContractAddress("0x0"));
    Community pageCommunity5 =  create(new Community().setStatus(PUBLIC).setName("page_community5").setTokenContractAddress("0x0"));
    Community pageCommunity6 =  create(new Community().setStatus(PUBLIC).setName("page_community6").setTokenContractAddress("0x0"));
    Community pageCommunity7 =  create(new Community().setStatus(PUBLIC).setName("page_community7").setTokenContractAddress("0x0"));
    Community pageCommunity8 =  create(new Community().setStatus(PUBLIC).setName("page_community8").setTokenContractAddress("0x0"));
    Community pageCommunity9 =  create(new Community().setStatus(PUBLIC).setName("page_community9").setTokenContractAddress("0x0"));
    Community pageCommunity10 =  create(new Community().setStatus(PUBLIC).setName("page_community10").setTokenContractAddress("0x0"));
    Community pageCommunity11 =  create(new Community().setStatus(PUBLIC).setName("page_community11").setTokenContractAddress("0x0"));
    Community pageCommunity12 =  create(new Community().setStatus(PUBLIC).setName("page_community12").setTokenContractAddress("0x0"));
    User pageUser = create(new User().setUsername("page_user").setPasswordHash(hash("password")).setEmailAddress("page_user@test.com").setCommunityUserList(asList(
        new CommunityUser().setCommunity(pageCommunity1), new CommunityUser().setCommunity(pageCommunity2),
        new CommunityUser().setCommunity(pageCommunity3), new CommunityUser().setCommunity(pageCommunity4),
        new CommunityUser().setCommunity(pageCommunity5), new CommunityUser().setCommunity(pageCommunity6),
        new CommunityUser().setCommunity(pageCommunity7), new CommunityUser().setCommunity(pageCommunity8),
        new CommunityUser().setCommunity(pageCommunity9), new CommunityUser().setCommunity(pageCommunity10),
        new CommunityUser().setCommunity(pageCommunity11), new CommunityUser().setCommunity(pageCommunity12))));

    sessionId = login("page_user", "password");
    
    // [non filter] page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community1", "page_community2", "page_community3", "page_community4", "page_community5",
          "page_community6", "page_community7", "page_community8", "page_community9", "page_community10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));
    
    // [non filter] page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community11", "page_community12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));
    
    // [non filter] page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community12", "page_community11", "page_community10", "page_community9", "page_community8",
          "page_community7", "page_community6", "page_community5", "page_community4", "page_community3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
    
    // [non filter] page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community2", "page_community1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
  
  @Test
  public void searchCommutity_filter_pagination() throws Exception {
    // prepare
    Community pageCommunity1 =  create(new Community().setStatus(PUBLIC).setName("page_community1").setTokenContractAddress("0x0"));
    Community pageCommunity2 =  create(new Community().setStatus(PUBLIC).setName("page_community2").setTokenContractAddress("0x0"));
    Community pageCommunity3 =  create(new Community().setStatus(PUBLIC).setName("page_community3").setTokenContractAddress("0x0"));
    Community pageCommunity4 =  create(new Community().setStatus(PUBLIC).setName("page_community4").setTokenContractAddress("0x0"));
    Community pageCommunity5 =  create(new Community().setStatus(PUBLIC).setName("page_community5").setTokenContractAddress("0x0"));
    Community pageCommunity6 =  create(new Community().setStatus(PUBLIC).setName("page_community6").setTokenContractAddress("0x0"));
    Community pageCommunity7 =  create(new Community().setStatus(PUBLIC).setName("page_community7").setTokenContractAddress("0x0"));
    Community pageCommunity8 =  create(new Community().setStatus(PUBLIC).setName("page_community8").setTokenContractAddress("0x0"));
    Community pageCommunity9 =  create(new Community().setStatus(PUBLIC).setName("page_community9").setTokenContractAddress("0x0"));
    Community pageCommunity10 =  create(new Community().setStatus(PUBLIC).setName("page_community10").setTokenContractAddress("0x0"));
    Community pageCommunity11 =  create(new Community().setStatus(PUBLIC).setName("page_community11").setTokenContractAddress("0x0"));
    Community pageCommunity12 =  create(new Community().setStatus(PUBLIC).setName("page_community12").setTokenContractAddress("0x0"));
    User pageUser = create(new User().setUsername("page_user").setPasswordHash(hash("password")).setEmailAddress("page_user@test.com").setCommunityUserList(asList(
        new CommunityUser().setCommunity(pageCommunity1), new CommunityUser().setCommunity(pageCommunity2),
        new CommunityUser().setCommunity(pageCommunity3), new CommunityUser().setCommunity(pageCommunity4),
        new CommunityUser().setCommunity(pageCommunity5), new CommunityUser().setCommunity(pageCommunity6),
        new CommunityUser().setCommunity(pageCommunity7), new CommunityUser().setCommunity(pageCommunity8),
        new CommunityUser().setCommunity(pageCommunity9), new CommunityUser().setCommunity(pageCommunity10),
        new CommunityUser().setCommunity(pageCommunity11), new CommunityUser().setCommunity(pageCommunity12))));

    sessionId = login("page_user", "password");
    
    // [non filter] page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "page", "0", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community1", "page_community2", "page_community3", "page_community4", "page_community5",
          "page_community6", "page_community7", "page_community8", "page_community9", "page_community10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));
    
    // [non filter] page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "page", "1", "10", "ASC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community11", "page_community12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));
    
    // [non filter] page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "page", "0", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community12", "page_community11", "page_community10", "page_community9", "page_community8",
          "page_community7", "page_community6", "page_community5", "page_community4", "page_community3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
    
    // [non filter] page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/users/{id}/communities?filter={filter}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", pageUser.getId(), "page", "1", "10", "DESC")
      .then().statusCode(200)
      .body("communityList.name", contains(
          "page_community2", "page_community1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
