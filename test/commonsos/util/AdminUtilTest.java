package commonsos.util;

import static commonsos.TestId.id;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class AdminUtilTest {

  @Test
  public void isCreatable() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1_1 = new Admin().setId(id("admin_com1_1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1_1 = new Admin().setId(id("teller_com1_1")).setRole(TELLER).setCommunity(com1);
    Admin admin_none_1 = new Admin().setId(id("admin_none_1")).setRole(COMMUNITY_ADMIN);
    Admin teller_none_1 = new Admin().setId(id("teller_none_1")).setRole(TELLER);
    
    // [ncl] execute & verify
    assertTrue(AdminUtil.isCreatable(ncl, null, NCL.getId()));
    assertTrue(AdminUtil.isCreatable(ncl, null, COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isCreatable(ncl, id("com1"), COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isCreatable(ncl, null, TELLER.getId()));
    assertTrue(AdminUtil.isCreatable(ncl, id("com1"), TELLER.getId()));

    // [admin_com1] execute & verify
    assertFalse(AdminUtil.isCreatable(admin_com1_1, null, NCL.getId()));
    assertTrue(AdminUtil.isCreatable(admin_com1_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(admin_com1_1, id("com2"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(admin_com1_1, null, COMMUNITY_ADMIN.getId()));
    assertTrue(AdminUtil.isCreatable(admin_com1_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatable(admin_com1_1, id("com2"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatable(admin_com1_1, null, TELLER.getId()));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isCreatable(admin_none_1, null, NCL.getId()));
    assertFalse(AdminUtil.isCreatable(admin_none_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(admin_none_1, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(admin_none_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatable(admin_none_1, null, TELLER.getId()));

    // [teller_com1] execute & verify
    assertFalse(AdminUtil.isCreatable(teller_com1_1, null, NCL.getId()));
    assertFalse(AdminUtil.isCreatable(teller_com1_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(teller_com1_1, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(teller_com1_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatable(teller_com1_1, null, TELLER.getId()));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isCreatable(teller_none_1, null, NCL.getId()));
    assertFalse(AdminUtil.isCreatable(teller_none_1, id("com1"), COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(teller_none_1, null, COMMUNITY_ADMIN.getId()));
    assertFalse(AdminUtil.isCreatable(teller_none_1, id("com1"), TELLER.getId()));
    assertFalse(AdminUtil.isCreatable(teller_none_1, null, TELLER.getId()));
  }
  
  @Test
  public void isSeeable() {
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
    assertTrue(AdminUtil.isSeeable(ncl, ncl));
    assertTrue(AdminUtil.isSeeable(ncl, admin_com1_1));
    assertTrue(AdminUtil.isSeeable(ncl, teller_com1_1));

    // [admin_com1] execute & verify
    assertFalse(AdminUtil.isSeeable(admin_com1_1, ncl));
    assertTrue(AdminUtil.isSeeable(admin_com1_1, admin_com1_1));
    assertTrue(AdminUtil.isSeeable(admin_com1_1, admin_com1_2));
    assertFalse(AdminUtil.isSeeable(admin_com1_1, admin_com2_1));
    assertFalse(AdminUtil.isSeeable(admin_com1_1, admin_none_1));
    assertTrue(AdminUtil.isSeeable(admin_com1_1, teller_com1_1));
    assertFalse(AdminUtil.isSeeable(admin_com1_1, teller_com2_1));
    assertFalse(AdminUtil.isSeeable(admin_com1_1, teller_none_1));

    // [admin_none] execute & verify
    assertFalse(AdminUtil.isSeeable(admin_none_1, ncl));
    assertTrue(AdminUtil.isSeeable(admin_none_1, admin_none_1));
    assertFalse(AdminUtil.isSeeable(admin_none_1, admin_com1_1));
    assertFalse(AdminUtil.isSeeable(admin_none_1, admin_none_2));
    assertFalse(AdminUtil.isSeeable(admin_none_1, teller_com1_1));
    assertFalse(AdminUtil.isSeeable(admin_none_1, teller_none_1));

    // [teller_com1] execute & verify
    assertFalse(AdminUtil.isSeeable(teller_com1_1, ncl));
    assertFalse(AdminUtil.isSeeable(teller_com1_1, admin_com1_1));
    assertFalse(AdminUtil.isSeeable(teller_com1_1, admin_none_1));
    assertTrue(AdminUtil.isSeeable(teller_com1_1, teller_com1_1));
    assertTrue(AdminUtil.isSeeable(teller_com1_1, teller_com1_2));
    assertFalse(AdminUtil.isSeeable(teller_com1_1, teller_com2_1));
    assertFalse(AdminUtil.isSeeable(teller_com1_1, teller_none_1));

    // [teller_none] execute & verify
    assertFalse(AdminUtil.isSeeable(teller_none_1, ncl));
    assertFalse(AdminUtil.isSeeable(teller_none_1, admin_com1_1));
    assertFalse(AdminUtil.isSeeable(teller_none_1, admin_none_1));
    assertTrue(AdminUtil.isSeeable(teller_none_1, teller_none_1));
    assertFalse(AdminUtil.isSeeable(teller_none_1, teller_com1_1));
    assertFalse(AdminUtil.isSeeable(teller_none_1, teller_none_2));
  }
}