package commonsos.integration.app.user;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class GetUserSearchTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setStatus(PUBLIC).setName("community"));
    otherCommunity =  create(new Community().setStatus(PUBLIC).setName("otherCommunity"));
    create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    create(new User().setUsername("otherUser1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    create(new User().setUsername("otherUser2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    create(new User().setUsername("otherCommunityUser1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));

    sessionId = loginApp("user1", "pass");
  }
  
  @Test
  public void userSearch() {
    // call api (filter)
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users?communityId={communityId}&q={q}", APP_API_VERSION.getMajor(), community.getId(), "user1")
      .then().statusCode(200)
      .body("userList.username", contains("otherUser1"))
      .body("userList.communityList.name", contains(contains("community")))
      .body("userList.communityList.walletLastViewTime", contains(contains(nullValue())));

    // call api (non filter)
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users?communityId={communityId}", APP_API_VERSION.getMajor(), community.getId())
      .then().statusCode(200)
      .body("userList.username", contains("otherUser1", "otherUser2"));
  }

  @Test
  public void userSearch_otherCommunity() {
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users?communityId={communityId}&q={q}", APP_API_VERSION.getMajor(), otherCommunity.getId(), "user")
      .then().statusCode(200)
      .body("userList.username", contains("otherCommunityUser1"));
  }

  @Test
  public void userSearch_pagination() throws Exception {
    // prepare
    Community pageCommunity =  create(new Community().setStatus(PUBLIC).setName("page_community"));
    create(new User().setUsername("page_user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user4").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user5").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user6").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user7").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user8").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user9").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user10").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user11").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));
    create(new User().setUsername("page_user12").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(pageCommunity))));

    sessionId = loginApp("user1", "pass");
    
    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users?communityId={communityId}&q={q}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "0", "10", "ASC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "page_user1", "page_user2", "page_user3", "page_user4", "page_user5",
          "page_user6", "page_user7", "page_user8", "page_user9", "page_user10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users?communityId={communityId}&q={q}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "1", "10", "ASC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "page_user11", "page_user12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));
    
    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users?communityId={communityId}&q={q}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "0", "10", "DESC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "page_user12", "page_user11", "page_user10", "page_user9", "page_user8",
          "page_user7", "page_user6", "page_user5", "page_user4", "page_user3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/app/v{v}/users?communityId={communityId}&q={q}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", APP_API_VERSION.getMajor(), pageCommunity.getId(), "page", "1", "10", "DESC")
      .then().statusCode(200)
      .body("userList.username", contains(
          "page_user2", "page_user1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}
