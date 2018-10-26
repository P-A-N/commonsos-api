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
    if (CollectionUtils.isEmpty(admin.getCommunityList())
        || CollectionUtils.isEmpty(user.getCommunityList())) {
      return false;
    }
    
    return user.getCommunityList().stream().anyMatch(community -> 
      community.getAdminUser() != null && community.getAdminUser().getId().equals(admin.getId())
    );
  }

  public static boolean isMember(User user, Community community) {
    return isMember(user, community.getId());
  }

  public static boolean isMember(User user, Long communityId) {
    return user.getCommunityList().stream().anyMatch(c -> c.getId().equals(communityId));
  }
}
