package commonsos.util;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.view.AdView;

public class AdUtil {
  
  private AdUtil() {}

  public static boolean isOwnAd(User user, Ad ad) {
    return ad.getCreatedUserId().equals(user.getId());
  }

  public static AdView viewForApp(Ad ad, User createdBy, User user) {
    return new AdView()
      .setId(ad.getId())
      .setCommunityId(ad.getCommunityId())
      .setCreatedBy(UserUtil.publicViewForApp(createdBy))
      .setType(ad.getType())
      .setTitle(ad.getTitle())
      .setDescription(ad.getDescription())
      .setPoints(ad.getPoints())
      .setLocation(ad.getLocation())
      .setOwn(AdUtil.isOwnAd(user, ad))
      .setCreatedAt(ad.getCreatedAt())
      .setPhotoUrl(ad.getPhotoUrl());
  }

  public static AdView viewForAdmin(Ad ad, User createdBy, Community community) {
    return new AdView()
      .setId(ad.getId())
      .setCommunity(CommunityUtil.narrowViewForAdmin(community))
      .setPublishStatus(ad.getPublishStatus())
      .setStatus(ad.getStatus())
      .setCreatedBy(UserUtil.narrowViewForAdmin(createdBy))
      .setType(ad.getType())
      .setTitle(ad.getTitle())
      .setDescription(ad.getDescription())
      .setPoints(ad.getPoints())
      .setLocation(ad.getLocation())
      .setCreatedAt(ad.getCreatedAt())
      .setPhotoUrl(ad.getPhotoUrl());
  }
}
