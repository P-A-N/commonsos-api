package commonsos.util;

import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;

import commonsos.repository.entity.Admin;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Role;
import commonsos.repository.entity.User;
import commonsos.view.admin.AdminView;

public class AdminUtil {
  
  private AdminUtil() {}

  public static AdminView toView(Admin admin) {
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
  
  public static boolean isCreatableAdmin(Admin admin, Long communityId, Long roleId) {
    Role adminRole = Role.of(admin.getRole().getId());
    Role targetRole = Role.of(roleId);
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole == NCL) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(communityId)) return false;
    
    if (adminRole == COMMUNITY_ADMIN) {
      if (targetRole == COMMUNITY_ADMIN || targetRole == TELLER) return true;
    }
    
    return false;
  }
  
  public static boolean isCreatableTokenTransaction(Admin admin, Long targetCommunityId) {
    Role adminRole = Role.of(admin.getRole().getId());
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole == NCL) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(targetCommunityId)) return false;
    
    if (adminRole == COMMUNITY_ADMIN) return true;
    
    return false;
  }
  
  public static boolean isSeeableAdmin(Admin admin, Admin target) {
    Role adminRole = Role.of(admin.getRole().getId());
    Role targetRole = Role.of(target.getRole().getId());
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    Long targetCommunityId = target.getCommunity() == null ? null : target.getCommunity().getId();
    
    if (adminRole == NCL) return true;
    if (admin.getId().equals(target.getId())) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(targetCommunityId)) return false;
    
    if (adminRole == COMMUNITY_ADMIN) {
      if (targetRole == COMMUNITY_ADMIN || targetRole == TELLER) return true;
    }
    if (adminRole == TELLER) {
      if (targetRole == TELLER) return true;
    }
    return false;
  }
  
  public static boolean isSeeableAdmin(Admin admin, Long targetCommunityId, Long targetRoleId) {
    Role adminRole = Role.of(admin.getRole().getId());
    Role targetRole = Role.of(targetRoleId);
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole == NCL) return true;
    if (adminCommunityId == null || !adminCommunityId.equals(targetCommunityId)) return false;
    
    if (adminRole == COMMUNITY_ADMIN) {
      if (targetRole == COMMUNITY_ADMIN || targetRole == TELLER) return true;
    }

    if (adminRole == TELLER) {
      if (targetRole == TELLER) return true;
    }
    
    return false;
  }
  
  public static boolean isSeeableUser(Admin admin, User target) {
    Role adminRole = Role.of(admin.getRole().getId());
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole == NCL) return true;
    
    if (target.getCommunityUserList().stream().map(CommunityUser::getCommunity).anyMatch(c -> c.getId().equals(adminCommunityId))) return true;

    return false;
  }
  
  public static boolean isSeeableCommunity(Admin admin, Long communityId) {
    Role adminRole = Role.of(admin.getRole().getId());
    Long adminCommunityId = admin.getCommunity() == null ? null : admin.getCommunity().getId();
    
    if (adminRole == NCL) return true;
    
    if (communityId != null && communityId.equals(adminCommunityId)) return true;

    return false;
  }
}
