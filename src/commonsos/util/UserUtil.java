package commonsos.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.TokenBalance;
import commonsos.view.UserTokenBalanceView;
import commonsos.view.UserView;
import commonsos.view.app.CommunityUserView;
import spark.utils.CollectionUtils;

public class UserUtil {
  
  private UserUtil() {}

  public static UserView publicViewForApp(User user) {
    if (user.getId().equals(MessageUtil.getSystemMessageCreatorId())) {
      return new UserView()
          .setId(MessageUtil.getSystemMessageCreatorId())
          .setUsername(MessageUtil.getSystemMessageCreatorUsername());
    }
    
    return new UserView()
        .setId(user.getId())
        .setFullName(fullName(user))
        .setUsername(user.getUsername())
        .setStatus(user.getStatus())
        .setLocation(user.getLocation())
        .setDescription(user.getDescription())
        .setAvatarUrl(user.getAvatarUrl());
  }
  
  public static UserView publicViewForApp(User user, List<CommunityUserView> communityList) {
    return publicViewForApp(user)
        .setCommunityList(communityList);
  }

  public static UserView privateViewForApp(User user, List<UserTokenBalanceView> balanceList, List<CommunityUserView> communityUserList) {
    return new UserView()
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
  
  public static CommunityUserView communityUserViewForApp(CommunityUser communityUser, TokenBalance tokenBalance) {
    Long adminUserId = communityUser.getCommunity().getAdminUser() == null ? null : communityUser.getCommunity().getAdminUser().getId();
    CommunityUserView view = new CommunityUserView();
    view.setBalance(tokenBalance.getBalance())
        .setWalletLastViewTime(communityUser.getWalletLastViewTime())
        .setAdLastViewTime(communityUser.getAdLastViewTime())
        .setNotificationLastViewTime(communityUser.getNotificationLastViewTime())
        .setId(communityUser.getCommunity().getId())
        .setName(communityUser.getCommunity().getName())
        .setAdminUserId(adminUserId)
        .setDescription(communityUser.getCommunity().getDescription())
        .setTokenSymbol(tokenBalance.getToken().getTokenSymbol())
        .setPhotoUrl(communityUser.getCommunity().getPhotoUrl())
        .setCoverPhotoUrl(communityUser.getCommunity().getCoverPhotoUrl())
        .setTransactionFee(communityUser.getCommunity().getFee());
    return view;
  }

  public static UserView wideViewForAdmin(User user, List<UserTokenBalanceView> balanceList) {
    return new UserView()
      .setId(user.getId())
      .setUsername(user.getUsername())
      .setStatus(user.getStatus())
      .setTelNo(user.getTelNo())
      .setCommunityList(communityUserViewListForAdmin(user.getCommunityUserList(), balanceList))
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress())
      .setLoggedinAt(user.getLoggedinAt())
      .setCreatedAt(user.getCreatedAt());
  }

  public static UserView narrowViewForAdmin(User user) {
    return new UserView()
      .setId(user.getId())
      .setUsername(user.getUsername());
  }
  
  public static List<CommunityUserView> communityUserViewListForAdmin(List<CommunityUser> communityUserList, List<UserTokenBalanceView> balanceList) {
    List<CommunityUserView> list = new ArrayList<>();
    communityUserList.stream().map(CommunityUser::getCommunity).forEach(c -> {
      UserTokenBalanceView balance = balanceList.stream().filter(b -> b.getCommunityId().equals(c.getId())).findFirst().get();
      CommunityUserView view = new CommunityUserView();
      view.setBalance(balance.getBalance())
          .setId(c.getId())
          .setName(c.getName())
          .setTokenSymbol(balance.getTokenSymbol());
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
