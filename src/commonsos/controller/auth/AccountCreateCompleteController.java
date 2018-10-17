package commonsos.controller.auth;

import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;

import commonsos.service.community.CommunityView;
import commonsos.service.transaction.BalanceView;
import commonsos.service.user.UserPrivateView;
import spark.Request;
import spark.Response;
import spark.Route;

public class AccountCreateCompleteController implements Route {

  @Override public UserPrivateView handle(Request request, Response response) {
    UserPrivateView privateView = new UserPrivateView()
        .setId(99990L)
        .setFullName("Test User")
        .setFirstName("User")
        .setLastName("Test")
        .setUsername("test")
        .setCommunityList(asList(
            new CommunityView().setId(999L).setName("testCommunity").setAdminUserId(99L),
            new CommunityView().setId(888L).setName("testCommunity2").setAdminUserId(88L)
            ))
        .setDescription("description")
        .setBalanceList(asList(
            new BalanceView().setCommunityId(999L).setBalance(TEN),
            new BalanceView().setCommunityId(888L).setBalance(TEN)
            ))
        .setLocation("location")
        .setAvatarUrl("avatarUrl")
        .setEmailAddress("test@test.com");
    
    return privateView;
  }
}
