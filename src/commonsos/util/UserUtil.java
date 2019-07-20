package commonsos.util;

import java.util.List;
import java.util.Optional;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;
import commonsos.view.app.BalanceView;
import commonsos.view.app.CommunityUserView;
import commonsos.view.app.CommunityView;
import commonsos.view.app.PrivateUserView;
import commonsos.view.app.PublicUserView;
import spark.utils.CollectionUtils;

public class UserUtil {
  
  private UserUtil() {}

  public static PublicUserView publicView(User user) {
    return new PublicUserView()
        .setId(user.getId())
        .setFullName(fullName(user))
        .setUsername(user.getUsername())
        .setStatus(user.getStatus())
        .setLocation(user.getLocation())
        .setDescription(user.getDescription())
        .setAvatarUrl(user.getAvatarUrl());
  }
  
  public static PublicUserView publicView(User user, List<CommunityView> communityList) {
    return publicView(user)
        .setCommunityList(communityList);
  }

  public static PrivateUserView privateView(User user, List<BalanceView> balanceList, List<CommunityUserView> communityUserList) {
    return new PrivateUserView()
      .setId(user.getId())
      .setBalanceList(balanceList)
      .setFullName(fullName(user))
      .setFirstName(user.getFirstName())
      .setLastName(user.getLastName())
      .setUsername(user.getUsername())
      .setStatus(user.getStatus())
      .setCommunityList(communityUserList)
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress());
  }
  
  public static CommunityUserView communityUserView(CommunityUser communityUser, String tokenSymbol) {
    Long adminUserId = communityUser.getCommunity().getAdminUser() == null ? null : communityUser.getCommunity().getAdminUser().getId();
    return new CommunityUserView()
        .setId(communityUser.getCommunity().getId())
        .setName(communityUser.getCommunity().getName())
        .setAdminUserId(adminUserId)
        .setDescription(communityUser.getCommunity().getDescription())
        .setTokenSymbol(tokenSymbol)
        .setPhotoUrl(communityUser.getCommunity().getPhotoUrl())
        .setCoverPhotoUrl(communityUser.getCommunity().getCoverPhotoUrl())
        .setWalletLastViewTime(communityUser.getWalletLastViewTime())
        .setAdLastViewTime(communityUser.getAdLastViewTime())
        .setNotificationLastViewTime(communityUser.getNotificationLastViewTime());
  }
  
  public static String fullName(User user) {
    return String.format("%s %s", user.getLastName(), user.getFirstName());
  }

  public static boolean isAdmin(User admin, Long communityId) {
    Optional<Community> community = admin.getCommunityUserList().stream().map(CommunityUser::getCommunity).filter(c -> c.getId().equals(communityId)).findFirst();
    return community.isPresent()
        && community.get().getAdminUser().getId().equals(admin.getId());
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
