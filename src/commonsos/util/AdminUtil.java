package commonsos.util;

import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Role;
import commonsos.repository.entity.User;
import commonsos.view.AdminView;

public class AdminUtil {
  
  private AdminUtil() {}

  public static AdminView view(Admin admin) {
    return new AdminView()
        .setId(admin.getId())
        .setAdminname(admin.getAdminname())
        .setCommunityId(admin.getCommunity() == null ? null : admin.getCommunity().getId())
        .setRoleId(admin.getRole() == null ? null : admin.getRole().getId())
        .setRolename(admin.getRole() == null ? null : admin.getRole().getRolename())
        .setEmailAddress(admin.getEmailAddress())
        .setTelNo(admin.getTelNo())
        .setDepartment(admin.getDepartment())
        .setPhotoUrl(admin.getPhotoUrl())
        .setLoggedinAt(admin.getLoggedinAt())
        .setCreatedAt(admin.getCreatedAt());
  }

  public static AdminView narrowView(Admin admin) {
    return new AdminView()
      .setId(admin.getId())
      .setAdminname(admin.getAdminname());
  }
  
  public static boolean isCreatableAdmin(Admin admin, Long communityId, Long roleId) {
    Role adminRole = admin.getRole();
    Role targetRole = Role.of(roleId);
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole.equals(NCL)) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(communityId)) return false;
    
    if (adminRole.equals(COMMUNITY_ADMIN)) {
      if (targetRole.equals(COMMUNITY_ADMIN) || targetRole.equals(TELLER)) return true;
    }
    
    return false;
  }
  
  public static boolean isCreatableEthTransaction(Admin admin) {
    Role adminRole = admin.getRole();
    if (adminRole.equals(NCL)) return true;
    return false;
  }
  
  public static boolean isCreatableTokenTransaction(Admin admin, Long targetCommunityId) {
    Role adminRole = admin.getRole();
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole.equals(NCL)) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(targetCommunityId)) return false;
    
    if (adminRole.equals(COMMUNITY_ADMIN)) return true;
    
    return false;
  }
  
  public static boolean isUpdatableAdmin(Admin admin, Admin targetAdmin) {
    Role adminRole = admin.getRole();
    Role targetRole = targetAdmin.getRole();
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    Long targetCommunityId = targetAdmin.getCommunity() == null ? null : targetAdmin.getCommunity().getId();

    if (admin.equals(targetAdmin)) return true;
    
    if (adminRole.equals(NCL)) {
      return targetRole.equals(COMMUNITY_ADMIN) || targetRole.equals(TELLER);
    }

    if (adminCommunityId == null || !adminCommunityId.equals(targetCommunityId)) return false;
    
    if (adminRole.equals(COMMUNITY_ADMIN)) {
      return targetRole.equals(TELLER);
    }
    
    return false;
  }
  
  public static boolean isUpdatableCommunity(Admin admin, Long targetCommunityId) {
    Role adminRole = admin.getRole();
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();

    if (adminRole.equals(NCL)) return true;

    if (adminRole.equals(COMMUNITY_ADMIN)) {
      if (adminCommunityId != null && adminCommunityId.equals(targetCommunityId)) return true;
    }
    
    return false;
  }
  
  public static boolean isSeeableAdmin(Admin admin, Admin target) {
    Role adminRole = admin.getRole();
    Role targetRole = target.getRole();
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    Long targetCommunityId = target.getCommunity() == null ? null : target.getCommunity().getId();
    
    if (adminRole.equals(NCL)) return true;
    if (admin.getId().equals(target.getId())) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(targetCommunityId)) return false;
    
    if (adminRole.equals(COMMUNITY_ADMIN)) {
      if (targetRole.equals(COMMUNITY_ADMIN) || targetRole.equals(TELLER)) return true;
    }
    if (adminRole.equals(TELLER)) {
      if (targetRole.equals(TELLER)) return true;
    }
    return false;
  }
  
  public static boolean isSeeableAdmin(Admin admin, Long targetCommunityId, Long targetRoleId) {
    Role adminRole = admin.getRole();
    Role targetRole = Role.of(targetRoleId);
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole.equals(NCL)) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(targetCommunityId)) return false;
    
    if (adminRole.equals(COMMUNITY_ADMIN)) {
      if (targetRole.equals(COMMUNITY_ADMIN) || targetRole.equals(TELLER)) return true;
    }

    if (adminRole.equals(TELLER)) {
      if (targetRole.equals(TELLER)) return true;
    }
    
    return false;
  }
  
  public static boolean isSeeableUser(Admin admin, User target) {
    Role adminRole = admin.getRole();
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole.equals(NCL)) return true;
    
    if (target.getCommunityUserList().stream().map(CommunityUser::getCommunity).anyMatch(c -> c.getId().equals(adminCommunityId))) return true;

    return false;
  }
  
  public static boolean isUpdatableUser(Admin admin, User target) {
    return isSeeableUser(admin, target);
  }
  
  public static boolean isSeeableAd(Admin admin, Long targetAdCommunityId) {
    Role adminRole = admin.getRole();
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole.equals(NCL)) return true;
    if (adminCommunityId != null && adminCommunityId.equals(targetAdCommunityId)) return true;

    return false;
  }
  
  public static boolean isUpdatableAd(Admin admin, Ad target) {
    return isSeeableAd(admin, target.getCommunityId());
  }
  
  public static boolean isSeeableCommunity(Admin admin, Long communityId) {
    Role adminRole = admin.getRole();
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole.equals(NCL)) return true;
    
    if (communityId != null && communityId.equals(adminCommunityId)) return true;

    return false;
  }
  
  public static boolean isSeeableCommunity(Admin admin, Long communityId, boolean isTellerSeeable) {
    if (!isTellerSeeable && admin.getRole().equals(TELLER)) return false;
    return isSeeableCommunity(admin, communityId);
  }
}
