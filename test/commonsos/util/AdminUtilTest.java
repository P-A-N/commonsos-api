package commonsos.util;

import static commonsos.TestId.id;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class AdminUtilTest {

  @Test
  public void isCreatableAdmin() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1_1 = new Admin().setId(id("admin_com1_1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1_1 = new Admin().setId(id("teller_com1_1")).setRole(TELLER).setCommunity(com1);
    Admin admin_none_1 = new Admin().setId(id("admin_none_1")).setRole(COMMUNITY_ADMIN);
    Admin teller_none_1 = new Admin().setId(id("teller_none_1")).setRole(TELLER);
    
    // [ncl] execute & verify
    assertTrue(AdminUtil.isCreatableAdmin(ncl, null, NCL.getId()));
    assertTrue(AdminUtil.isCreatableAdmin(ncl, null, COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isCreatableAdmin(ncl, id("com1"), COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isCreatableAdmin(ncl, null, TELLER.getId()));
    assertTrue(AdminUtil.isCreatableAdmin(ncl, id("com1"), TELLER.getId()));

    // [admin_com1] execute & verify
    assertFalse(AdminUtil.isCreatableAdmin(admin_com1_1, null, NCL.getId()));
    assertTrue(AdminUtil.isCreatableAdmin(admin_com1_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_com1_1, id("com2"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_com1_1, null, COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isCreatableAdmin(admin_com1_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_com1_1, id("com2"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_com1_1, null, TELLER.getId()));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isCreatableAdmin(admin_none_1, null, NCL.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_none_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_none_1, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_none_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(admin_none_1, null, TELLER.getId()));

    // [teller_com1] execute & verify
    assertFalse(AdminUtil.isCreatableAdmin(teller_com1_1, null, NCL.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_com1_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_com1_1, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_com1_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_com1_1, null, TELLER.getId()));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isCreatableAdmin(teller_none_1, null, NCL.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_none_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_none_1, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_none_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatableAdmin(teller_none_1, null, TELLER.getId()));
  }

  @Test
  public void isCreatableTokenTransaction() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Community com2 = new Community().setId(id("com2"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1 = new Admin().setId(id("admin_com1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1 = new Admin().setId(id("teller_com1")).setRole(TELLER).setCommunity(com1);
    Admin admin_none = new Admin().setId(id("admin_none")).setRole(COMMUNITY_ADMIN);
    Admin teller_none = new Admin().setId(id("teller_none")).setRole(TELLER);

    // [ncl] execute & verify
    assertTrue(AdminUtil.isCreatableTokenTransaction(ncl, com1.getId()));
    assertTrue(AdminUtil.isCreatableTokenTransaction(ncl, com2.getId()));

    // [admin_com1] execute & verify
    assertTrue(AdminUtil.isCreatableTokenTransaction(admin_com1, com1.getId()));
    assertFalse(AdminUtil.isCreatableTokenTransaction(admin_com1, com2.getId()));

    // [teller_com1] execute & verify
    assertFalse(AdminUtil.isCreatableTokenTransaction(teller_com1, com1.getId()));
    assertFalse(AdminUtil.isCreatableTokenTransaction(teller_com1, com2.getId()));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isCreatableTokenTransaction(admin_none, com1.getId()));
    assertFalse(AdminUtil.isCreatableTokenTransaction(admin_none, com2.getId()));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isCreatableTokenTransaction(teller_none, com1.getId()));
    assertFalse(AdminUtil.isCreatableTokenTransaction(teller_none, com2.getId()));
  }
  
  @Test
  public void isSeeableAdmin_forGet() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Community com2 = new Community().setId(id("com2"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1_1 = new Admin().setId(id("admin_com1_1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin admin_com1_2 = new Admin().setId(id("admin_com1_2")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1_1 = new Admin().setId(id("teller_com1_1")).setRole(TELLER).setCommunity(com1);
    Admin teller_com1_2 = new Admin().setId(id("teller_com1_2")).setRole(TELLER).setCommunity(com1);
    Admin admin_com2_1 = new Admin().setId(id("admin_com2_1")).setRole(COMMUNITY_ADMIN).setCommunity(com2);
    Admin teller_com2_1 = new Admin().setId(id("teller_com2_1")).setRole(TELLER).setCommunity(com2);
    Admin admin_none_1 = new Admin().setId(id("admin_none_1")).setRole(COMMUNITY_ADMIN);
    Admin admin_none_2 = new Admin().setId(id("admin_none_2")).setRole(COMMUNITY_ADMIN);
    Admin teller_none_1 = new Admin().setId(id("teller_none_1")).setRole(TELLER);
    Admin teller_none_2 = new Admin().setId(id("teller_none_2")).setRole(TELLER);
    
    // [ncl] execute & verify
    assertTrue(AdminUtil.isSeeableAdmin(ncl, ncl));
    assertTrue(AdminUtil.isSeeableAdmin(ncl, admin_com1_1));
    assertTrue(AdminUtil.isSeeableAdmin(ncl, teller_com1_1));

    // [admin_com1] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1_1, ncl));
    assertTrue(AdminUtil.isSeeableAdmin(admin_com1_1, admin_com1_1));
    assertTrue(AdminUtil.isSeeableAdmin(admin_com1_1, admin_com1_2));
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1_1, admin_com2_1));
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1_1, admin_none_1));
    assertTrue(AdminUtil.isSeeableAdmin(admin_com1_1, teller_com1_1));
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1_1, teller_com2_1));
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1_1, teller_none_1));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(admin_none_1, ncl));
    assertTrue(AdminUtil.isSeeableAdmin(admin_none_1, admin_none_1));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none_1, admin_com1_1));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none_1, admin_none_2));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none_1, teller_com1_1));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none_1, teller_none_1));

    // [teller_com1] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1_1, ncl));
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1_1, admin_com1_1));
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1_1, admin_none_1));
    assertTrue(AdminUtil.isSeeableAdmin(teller_com1_1, teller_com1_1));
    assertTrue(AdminUtil.isSeeableAdmin(teller_com1_1, teller_com1_2));
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1_1, teller_com2_1));
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1_1, teller_none_1));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(teller_none_1, ncl));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none_1, admin_com1_1));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none_1, admin_none_1));
    assertTrue(AdminUtil.isSeeableAdmin(teller_none_1, teller_none_1));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none_1, teller_com1_1));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none_1, teller_none_2));
  }

  @Test
  public void isSeeableAdmin_forSearch() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Community com2 = new Community().setId(id("com2"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1 = new Admin().setId(id("admin_com1_1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1 = new Admin().setId(id("teller_com1_1")).setRole(TELLER).setCommunity(com1);
    Admin admin_none = new Admin().setId(id("admin_none_1")).setRole(COMMUNITY_ADMIN);
    Admin teller_none = new Admin().setId(id("teller_none_1")).setRole(TELLER);
    
    // [ncl] execute & verify
    assertTrue(AdminUtil.isSeeableAdmin(ncl, com1.getId(), NCL.getId()));
    assertTrue(AdminUtil.isSeeableAdmin(ncl, com1.getId(), COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isSeeableAdmin(ncl, com1.getId(), TELLER.getId()));

    // [admin_com1] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1, com1.getId(), NCL.getId()));
    assertTrue(AdminUtil.isSeeableAdmin(admin_com1, com1.getId(), COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isSeeableAdmin(admin_com1, com1.getId(), TELLER.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1, com2.getId(), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(admin_com1, com2.getId(), TELLER.getId()));

    // [teller_com1] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1, com1.getId(), NCL.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1, com1.getId(), COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isSeeableAdmin(teller_com1, com1.getId(), TELLER.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1, com2.getId(), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(teller_com1, com2.getId(), TELLER.getId()));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(admin_none, com1.getId(), NCL.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none, com1.getId(), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none, com1.getId(), TELLER.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(admin_none, null, TELLER.getId()));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isSeeableAdmin(teller_none, com1.getId(), NCL.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none, com1.getId(), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none, com1.getId(), TELLER.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isSeeableAdmin(teller_none, null, TELLER.getId()));
  }

  @Test
  public void isSeeableUser() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Community com2 = new Community().setId(id("com2"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1 = new Admin().setId(id("admin_com1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1 = new Admin().setId(id("teller_com1")).setRole(TELLER).setCommunity(com1);
    Admin admin_none = new Admin().setId(id("admin_none")).setRole(COMMUNITY_ADMIN);
    Admin teller_none = new Admin().setId(id("teller_none")).setRole(TELLER);
    User user_com1 = new User().setId(id("user_com1")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1)));
    User user_com2 = new User().setId(id("user_com2")).setCommunityUserList(asList(new CommunityUser().setCommunity(com2)));

    // [ncl] execute & verify
    assertTrue(AdminUtil.isSeeableUser(ncl, user_com1));
    assertTrue(AdminUtil.isSeeableUser(ncl, user_com2));

    // [admin_com1] execute & verify
    assertTrue(AdminUtil.isSeeableUser(admin_com1, user_com1));
    assertFalse(AdminUtil.isSeeableUser(admin_com1, user_com2));

    // [teller_com1] execute & verify
    assertTrue(AdminUtil.isSeeableUser(teller_com1, user_com1));
    assertFalse(AdminUtil.isSeeableUser(teller_com1, user_com2));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isSeeableUser(admin_none, user_com1));
    assertFalse(AdminUtil.isSeeableUser(admin_none, user_com2));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isSeeableUser(teller_none, user_com1));
    assertFalse(AdminUtil.isSeeableUser(teller_none, user_com2));
  }

  @Test
  public void isSeeableCommunity() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Community com2 = new Community().setId(id("com2"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1 = new Admin().setId(id("admin_com1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1 = new Admin().setId(id("teller_com1")).setRole(TELLER).setCommunity(com1);
    Admin admin_none = new Admin().setId(id("admin_none")).setRole(COMMUNITY_ADMIN);
    Admin teller_none = new Admin().setId(id("teller_none")).setRole(TELLER);

    // [ncl] execute & verify
    assertTrue(AdminUtil.isSeeableCommunity(ncl, com1.getId()));
    assertTrue(AdminUtil.isSeeableCommunity(ncl, com2.getId()));

    // [admin_com1] execute & verify
    assertTrue(AdminUtil.isSeeableCommunity(admin_com1, com1.getId()));
    assertFalse(AdminUtil.isSeeableCommunity(admin_com1, com2.getId()));

    // [teller_com1] execute & verify
    assertTrue(AdminUtil.isSeeableCommunity(teller_com1, com1.getId()));
    assertFalse(AdminUtil.isSeeableCommunity(teller_com1, com2.getId()));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isSeeableCommunity(admin_none, com1.getId()));
    assertFalse(AdminUtil.isSeeableCommunity(admin_none, com2.getId()));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isSeeableCommunity(teller_none, com1.getId()));
    assertFalse(AdminUtil.isSeeableCommunity(teller_none, com2.getId()));
  }
}