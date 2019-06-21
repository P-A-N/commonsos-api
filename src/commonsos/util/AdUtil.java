package commonsos.util;

import static commonsos.repository.entity.AdType.GIVE;
import static commonsos.repository.entity.AdType.WANT;
import static java.math.BigDecimal.ZERO;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.User;
import commonsos.view.AdView;

public class AdUtil {
  
  private AdUtil() {}

  public static boolean isOwnAd(User user, Ad ad) {
    return ad.getCreatedBy().equals(user.getId());
  }

  public static boolean isPayableByUser(User user, Ad ad) {
    if (ZERO.compareTo(ad.getPoints()) >= 0) return false;
    if (isOwnAd(user, ad) && WANT == ad.getType()) return true;
    if (!isOwnAd(user, ad) && GIVE == ad.getType()) return true;
    return false;
  }

  public static AdView view(Ad ad, User createdBy, User user) {
    return new AdView()
      .setId(ad.getId())
      .setCommunityId(ad.getCommunityId())
      .setCreatedBy(UserUtil.view(createdBy))
      .setType(ad.getType())
      .setTitle(ad.getTitle())
      .setDescription(ad.getDescription())
      .setPoints(ad.getPoints())
      .setLocation(ad.getLocation())
      .setOwn(AdUtil.isOwnAd(user, ad))
      .setPayable(AdUtil.isPayableByUser(user, ad))
      .setCreatedAt(ad.getCreatedAt())
      .setPhotoUrl(ad.getPhotoUrl());
  }
}
