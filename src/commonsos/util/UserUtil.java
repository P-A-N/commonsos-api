package commonsos.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.TokenBalance;
import commonsos.view.UserTokenBalanceView;
import commonsos.view.admin.CommunityUserForAdminView;
import commonsos.view.admin.UserForAdminView;
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

  public static PrivateUserView privateView(User user, List<UserTokenBalanceView> balanceList, List<CommunityUserView> communityUserList) {
    return new PrivateUserView()
      .setId(user.getId())
      .setBalanceList(balanceList)
      .setFullName(fullName(user))
      .setFirstName(user.getFirstName())
      .setLastName(user.getLastName())
      .setUsername(user.getUsername())
      .setStatus(user.getStatus())
      .setTelNo(user.getTelNo())
      .setCommunityList(communityUserList)
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress())
      .setLoggedinAt(user.getLoggedinAt());
  }
  
  public static CommunityUserView communityUserView(CommunityUser communityUser, TokenBalance tokenBalance) {
    Long adminUserId = communityUser.getCommunity().getAdminUser() == null ? null : communityUser.getCommunity().getAdminUser().getId();
    return new CommunityUserView()
        .setId(communityUser.getCommunity().getId())
        .setName(communityUser.getCommunity().getName())
        .setAdminUserId(adminUserId)
        .setDescription(communityUser.getCommunity().getDescription())
        .setTokenSymbol(tokenBalance.getToken().getTokenSymbol())
        .setBalance(tokenBalance.getBalance())
        .setPhotoUrl(communityUser.getCommunity().getPhotoUrl())
        .setCoverPhotoUrl(communityUser.getCommunity().getCoverPhotoUrl())
        .setTransactionFee(communityUser.getCommunity().getFee())
        .setWalletLastViewTime(communityUser.getWalletLastViewTime())
        .setAdLastViewTime(communityUser.getAdLastViewTime())
        .setNotificationLastViewTime(communityUser.getNotificationLastViewTime());
  }

  public static UserForAdminView userForAdminView(User user, List<UserTokenBalanceView> balanceList) {
    return new UserForAdminView()
      .setId(user.getId())
      .setUsername(user.getUsername())
      .setStatus(user.getStatus())
      .setTelNo(user.getTelNo())
      .setCommunityList(communityUserForAdminViewList(user.getCommunityUserList(), balanceList))
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress())
      .setLoggedinAt(user.getLoggedinAt())
      .setCreatedAt(user.getCreatedAt());
  }
  
  public static List<CommunityUserForAdminView> communityUserForAdminViewList(List<CommunityUser> communityUserList, List<UserTokenBalanceView> balanceList) {
    List<CommunityUserForAdminView> list = new ArrayList<>();
    communityUserList.stream().map(CommunityUser::getCommunity).forEach(c -> {
      UserTokenBalanceView balance = balanceList.stream().filter(b -> b.getCommunityId().equals(c.getId())).findFirst().get();
      CommunityUserForAdminView view = new CommunityUserForAdminView()
          .setId(c.getId())
          .setName(c.getName())
          .setTokenSymbol(balance.getTokenSymbol())
          .setBalance(balance.getBalance());
      list.add(view);
    });
    return list;
  }
  
  public static String fullName(User user) {
    return String.format("%s %s", user.getLastName(), user.getFirstName());
  }

  public static boolean isAdmin(User admin, Long communityId) {
    Optional<Community> community = admin.getCommunityUserList().stream().map(CommunityUser::getCommunity).filter(c -> c.getId().equals(communityId)).findFirst();
    return community.isPresent()
        && community.get().getAdminUser() != null
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
