package commonsos.util;

import commonsos.repository.entity.Admin;
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
}
