package commonsos.util;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.User;
import commonsos.view.app.AdView;

public class AdUtil {
  
  private AdUtil() {}

  public static boolean isOwnAd(User user, Ad ad) {
    return ad.getCreatedUserId().equals(user.getId());
  }

  public static AdView view(Ad ad, User createdBy, User user) {
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
}
