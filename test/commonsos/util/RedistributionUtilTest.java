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

public class RedistributionUtilTest {

  @Test
  public void isEditable() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    Admin ncl = new Admin().setId(id("ncl")).setRole(NCL);
    Admin admin_com1 = new Admin().setId(id("admin_com1")).setRole(COMMUNITY_ADMIN).setCommunity(com1);
    Admin teller_com1 = new Admin().setId(id("teller_com1")).setRole(TELLER).setCommunity(com1);
    Admin admin_none = new Admin().setId(id("admin_none")).setRole(COMMUNITY_ADMIN);
    
    // [ncl] execute & verify
    assertTrue(RedistributionUtil.isEditable(ncl, id("com1")));

    // [admin_com1] execute & verify
    assertTrue(RedistributionUtil.isEditable(admin_com1, id("com1")));
    assertFalse(RedistributionUtil.isEditable(admin_com1, id("com2")));

    // [teller_com1] execute & verify
    assertFalse(RedistributionUtil.isEditable(teller_com1, id("com1")));
    assertFalse(RedistributionUtil.isEditable(teller_com1, id("com2")));

    // [admin_com1] execute & verify
    assertFalse(RedistributionUtil.isEditable(admin_none, id("com1")));
    assertFalse(RedistributionUtil.isEditable(admin_none, id("com2")));
  }
}