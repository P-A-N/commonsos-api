package commonsos.util;

import java.util.List;

import javax.inject.Singleton;

import commonsos.repository.community.Community;
import commonsos.repository.user.User;
import commonsos.service.community.CommunityView;
import commonsos.service.transaction.BalanceView;
import commonsos.service.user.UserPrivateView;
import commonsos.service.user.UserView;
import spark.utils.CollectionUtils;

@Singleton
public class UserUtil {

  public UserView view(User user) {
    return new UserView()
        .setId(user.getId())
        .setFullName(fullName(user))
        .setUsername(user.getUsername())
        .setLocation(user.getLocation())
        .setDescription(user.getDescription())
        .setAvatarUrl(user.getAvatarUrl());
  }

  public UserPrivateView privateView(User user, List<BalanceView> balanceList, List<CommunityView> communityList) {
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
  
  public String fullName(User user) {
    return String.format("%s %s", user.getLastName(), user.getFirstName());
  }

  public boolean isAdminOfUser(User admin, User user) {
    if (CollectionUtils.isEmpty(admin.getJoinedCommunities())
        || CollectionUtils.isEmpty(user.getJoinedCommunities())) {
      return false;
    }
    
    return user.getJoinedCommunities().stream().anyMatch(community -> 
      community.getAdminUser() != null && community.getAdminUser().getId().equals(admin.getId())
    );
  }

  public boolean isMember(User user, Community community) {
    return isMember(user, community.getId());
  }

  public boolean isMember(User user, Long communityId) {
    return user.getJoinedCommunities().stream().anyMatch(c -> c.getId().equals(communityId));
  }
}
