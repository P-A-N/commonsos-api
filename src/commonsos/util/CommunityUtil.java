package commonsos.util;

import commonsos.repository.entity.Community;
import commonsos.view.CommunityView;

public class CommunityUtil {
  
  private CommunityUtil() {}

  public static CommunityView view(Community community, String tokenSymbol) {
    Long adminUserId = community.getAdminUser() == null ? null : community.getAdminUser().getId();
    return new CommunityView()
        .setId(community.getId())
        .setName(community.getName())
        .setAdminUserId(adminUserId)
        .setDescription(community.getDescription())
        .setTokenSymbol(tokenSymbol)
        .setPhotoUrl(community.getPhotoUrl())
        .setCoverPhotoUrl(community.getCoverPhotoUrl());
  }
}
