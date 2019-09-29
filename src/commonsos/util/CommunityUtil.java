package commonsos.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityNotification;
import commonsos.service.blockchain.CommunityToken;
import commonsos.view.AdminView;
import commonsos.view.CommunityNotificationView;
import commonsos.view.CommunityUserView;
import commonsos.view.CommunityView;

public class CommunityUtil {
  
  private CommunityUtil() {}

  public static CommunityUserView viewForApp(Community community, String tokenSymbol) {
    Long adminUserId = community.getAdminUser() == null ? null : community.getAdminUser().getId();
    CommunityUserView view = new CommunityUserView();
    view.setId(community.getId())
        .setName(community.getName())
        .setAdminUserId(adminUserId)
        .setDescription(community.getDescription())
        .setTokenSymbol(tokenSymbol)
        .setPhotoUrl(community.getPhotoUrl())
        .setCoverPhotoUrl(community.getCoverPhotoUrl())
        .setTransactionFee(community.getFee());
    return view;
  }

  public static CommunityView viewForAdmin(
      Community community,
      CommunityToken token,
      Integer totalMember,
      List<Admin> adminList
      ) {
    List<AdminView> adminViewList = adminList
        .stream().map(AdminUtil::narrowView)
        .collect(Collectors.toList());
    
    return new CommunityView()
        .setCommunityId(community.getId())
        .setCommunityName(community.getName())
        .setTokenName(token.getTokenName())
        .setTokenSymbol(token.getTokenSymbol())
        .setTransactionFee(community.getFee())
        .setDescription(community.getDescription())
        .setStatus(community.getStatus().name())
        .setAdminPageUrl(community.getAdminPageUrl())
        .setTotalSupply(token.getTotalSupply())
        .setTotalMember(totalMember)
        .setPhotoUrl(community.getPhotoUrl())
        .setCoverPhotoUrl(community.getCoverPhotoUrl())
        .setCreatedAt(community.getCreatedAt())
        .setAdminList(adminViewList);
  }

  public static List<CommunityNotificationView> notificationView(List<CommunityNotification> notificationList) {
    List<CommunityNotificationView> notificationViewList = new ArrayList<>();
    notificationList.forEach(n -> notificationViewList.add(notificationView(n)));
    
    return notificationViewList;
  }

  public static CommunityNotificationView notificationView(CommunityNotification notification) {
    return new CommunityNotificationView()
        .setWordpressId(notification.getWordpressId())
        .setUpdatedAt(notification.getUpdatedNotificationAt());
  }
}
