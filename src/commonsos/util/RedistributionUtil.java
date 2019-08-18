package commonsos.util;

import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;

import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.Role;
import commonsos.view.admin.RedistributionView;

public class RedistributionUtil {
  
  private RedistributionUtil() {}
  
  public static RedistributionView toView(Redistribution r) {
    return new RedistributionView()
        .setRedistributionId(r.getId())
        .setIsAll(r.isAll())
        .setUserId(r.getUser() == null ? null : r.getUser().getId())
        .setUsername(r.getUser() == null ? null : r.getUser().getUsername())
        .setRedistributionRate(r.getRate());
  }

  public static boolean isEditable(Admin admin, Long communityId) {
    Role adminRole = Role.of(admin.getRole().getId());
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole == NCL) return true;
    if (adminRole == COMMUNITY_ADMIN && communityId.equals(adminCommunityId)) return true;
    
    return false;
  }
}
