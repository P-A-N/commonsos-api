package commonsos.util;

import java.util.List;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.view.BalanceView;
import commonsos.view.CommunityView;
import commonsos.view.UserPrivateView;
import commonsos.view.UserView;
import spark.utils.CollectionUtils;

public class UserUtil {
  
  private UserUtil() {}

  public static UserView view(User user) {
    return new UserView()
        .setId(user.getId())
        .setFullName(fullName(user))
        .setUsername(user.getUsername())
        .setStatus(user.getStatus())
        .setLocation(user.getLocation())
        .setDescription(user.getDescription())
        .setAvatarUrl(user.getAvatarUrl());
  }

  public static UserPrivateView privateView(User user, List<BalanceView> balanceList, List<CommunityView> communityList) {
    return new UserPrivateView()
      .setId(user.getId())
      .setBalanceList(balanceList)
      .setFullName(fullName(user))
      .setFirstName(user.getFirstName())
      .setLastName(user.getLastName())
      .setUsername(user.getUsername())
      .setStatus(user.getStatus())
      .setCommunityList(communityList)
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress());
  }
  
  public static String fullName(User user) {
    return String.format("%s %s", user.getLastName(), user.getFirstName());
  }

  public static boolean isAdminOfUser(User admin, User user) {
    if (CollectionUtils.isEmpty(admin.getCommunityUserList())
        || CollectionUtils.isEmpty(user.getCommunityUserList())) {
      return false;
    }
    
    return user.getCommunityUserList().stream().anyMatch(cu -> 
      cu.getCommunity().getAdminUser() != null && cu.getCommunity().getAdminUser().getId().equals(admin.getId())
    );
  }

  public static boolean isMember(User user, Community community) {
    return isMember(user, community.getId());
  }

  public static boolean isMember(User user, Long communityId) {
    return user.getCommunityUserList().stream().anyMatch(cu -> cu.getCommunity().getId().equals(communityId));
  }
}
