package commonsos.repository;

import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.TemporaryAdmin;
import commonsos.repository.entity.TemporaryAdminEmailAddress;

public class AdminRepositoryTest extends AbstractRepositoryTest {

  private AdminRepository repository = spy(new AdminRepository(emService));
  private CommunityRepository communityRepository = spy(new CommunityRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
    doNothing().when(communityRepository).checkLocked(any());
  }
  
  @Test
  public void findById() {
    // prepare
    Admin adm1 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm1")));
    Admin adm2 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm2").setDeleted(true)));

    // execute & verify
    Optional<Admin> result = repository.findById(adm1.getId());
    assertThat(result.get().getEmailAddress()).isEqualTo("adm1");

    // execute & verify
    result = repository.findById(adm2.getId());
    assertFalse(result.isPresent());

    // execute & verify
    result = repository.findById(-1L);
    assertFalse(result.isPresent());
  }

  @Test
  public void findByCommunityId() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    Community com3 = inTransaction(() -> communityRepository.create(new Community().setName("com3")));
    Admin adm1 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm1").setCommunity(com1)));
    Admin adm2 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm2").setCommunity(com1)));
    Admin adm3 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm3").setCommunity(com1).setDeleted(true)));
    Admin adm4 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm4").setCommunity(com2)));

    // execute & verify
    List<Admin> result = repository.findByCommunityId(com1.getId(), null).getList();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getId()).isEqualTo(adm1.getId());
    assertThat(result.get(1).getId()).isEqualTo(adm2.getId());

    // execute & verify
    result = repository.findByCommunityId(com2.getId(), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getId()).isEqualTo(adm4.getId());

    // execute & verify
    result = repository.findByCommunityId(com3.getId(), null).getList();
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void findByCommunityId_pagination() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm1").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm2").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm3").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm4").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm5").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm6").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm7").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm8").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm9").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm10").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm11").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setEmailAddress("adm12").setCommunity(com1)));

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(10).setSort(SortType.ASC);
    List<Admin> result = repository.findByCommunityId(com1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(10);

    // execute & verify
    pagination.setPage(1);
    result = repository.findByCommunityId(com1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  public void findByCommunityIdAndRoleId() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    Admin ncl = inTransaction(() -> repository.create(new Admin().setRole(NCL).setEmailAddress("ncl")));
    Admin comAd1 = inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("comAd1").setCommunity(com1)));
    Admin comAd2 = inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("comAd2").setCommunity(com1)));
    Admin comAd3 = inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("comAd2").setCommunity(com1).setDeleted(true)));
    Admin comAd4 = inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("comAd4")));
    Admin teller1 = inTransaction(() -> repository.create(new Admin().setRole(TELLER).setEmailAddress("teller1").setCommunity(com1)));
    Admin teller2 = inTransaction(() -> repository.create(new Admin().setRole(TELLER).setEmailAddress("teller2").setCommunity(com1)));
    Admin teller3 = inTransaction(() -> repository.create(new Admin().setRole(TELLER).setEmailAddress("teller3").setCommunity(com1).setDeleted(true)));
    Admin teller4 = inTransaction(() -> repository.create(new Admin().setRole(TELLER).setEmailAddress("teller4")));

    // execute & verify
    List<Admin> result = repository.findByCommunityIdAndRoleId(null, NCL.getId(), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getId()).isEqualTo(ncl.getId());
    
    // execute & verify
    result = repository.findByCommunityIdAndRoleId(com1.getId(), COMMUNITY_ADMIN.getId(), null).getList();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getId()).isEqualTo(comAd1.getId());
    assertThat(result.get(1).getId()).isEqualTo(comAd2.getId());
    
    // execute & verify
    result = repository.findByCommunityIdAndRoleId(com2.getId(), COMMUNITY_ADMIN.getId(), null).getList();
    assertThat(result.size()).isEqualTo(0);
    
    // execute & verify
    result = repository.findByCommunityIdAndRoleId(null, COMMUNITY_ADMIN.getId(), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getId()).isEqualTo(comAd4.getId());
    
    // execute & verify
    result = repository.findByCommunityIdAndRoleId(com1.getId(), TELLER.getId(), null).getList();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getId()).isEqualTo(teller1.getId());
    assertThat(result.get(1).getId()).isEqualTo(teller2.getId());
    
    // execute & verify
    result = repository.findByCommunityIdAndRoleId(com2.getId(), TELLER.getId(), null).getList();
    assertThat(result.size()).isEqualTo(0);
    
    // execute & verify
    result = repository.findByCommunityIdAndRoleId(null, TELLER.getId(), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getId()).isEqualTo(teller4.getId());
  }

  @Test
  public void findByCommunityIdAndRoleId_pagination() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm1").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm2").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm3").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm4").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm5").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm6").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm7").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm8").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm9").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm10").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm11").setCommunity(com1)));
    inTransaction(() -> repository.create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("adm12").setCommunity(com1)));

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(10).setSort(SortType.ASC);
    List<Admin> result = repository.findByCommunityIdAndRoleId(com1.getId(), COMMUNITY_ADMIN.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(10);

    // execute & verify
    pagination.setPage(1);
    result = repository.findByCommunityIdAndRoleId(com1.getId(), COMMUNITY_ADMIN.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(2);
  }


  @Test
  public void findByEmailAddress() {
    // prepare
    Admin adm1 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm1")));
    Admin adm2 = inTransaction(() -> repository.create(new Admin().setEmailAddress("adm2").setDeleted(true)));

    // execute & verify
    Optional<Admin> result = repository.findByEmailAddress("adm1");
    assertThat(result.get().getId()).isEqualTo(adm1.getId());

    // execute & verify
    result = repository.findByEmailAddress("adm2");
    assertFalse(result.isPresent());

    // execute & verify
    result = repository.findByEmailAddress("hoge");
    assertFalse(result.isPresent());
  }

  @Test
  public void isEmailAddressTaken() {
    // prepare valid
    inTransaction(() -> repository.create(new Admin().setEmailAddress("e1@test.com")));
    inTransaction(() -> repository.createTemporary(new TemporaryAdmin().setEmailAddress("e2@test.com").setAccessIdHash("").setInvalid(false).setExpirationTime(Instant.now().plusSeconds(600))));
    inTransaction(() -> repository.createTemporaryEmail(new TemporaryAdminEmailAddress().setEmailAddress("e3@test.com").setAccessIdHash("").setInvalid(false).setExpirationTime(Instant.now().plusSeconds(600))));
    // prepare invalid
    inTransaction(() -> repository.create(new Admin().setEmailAddress("e4@test.com").setDeleted(true)));
    inTransaction(() -> repository.createTemporary(new TemporaryAdmin().setEmailAddress("e4@test.com").setAccessIdHash("").setInvalid(true).setExpirationTime(Instant.now().plusSeconds(600))));
    inTransaction(() -> repository.createTemporary(new TemporaryAdmin().setEmailAddress("e4@test.com").setAccessIdHash("").setInvalid(false).setExpirationTime(Instant.now().minusSeconds(600))));
    inTransaction(() -> repository.createTemporaryEmail(new TemporaryAdminEmailAddress().setEmailAddress("e4@test.com").setAccessIdHash("").setInvalid(true).setExpirationTime(Instant.now().plusSeconds(600))));
    inTransaction(() -> repository.createTemporaryEmail(new TemporaryAdminEmailAddress().setEmailAddress("e4@test.com").setAccessIdHash("").setInvalid(false).setExpirationTime(Instant.now().minusSeconds(600))));

    // execute & verify
    assertTrue(repository.isEmailAddressTaken("e1@test.com"));
    assertTrue(repository.isEmailAddressTaken("e2@test.com"));
    assertTrue(repository.isEmailAddressTaken("e3@test.com"));
    assertFalse(repository.isEmailAddressTaken("e4@test.com"));
    assertFalse(repository.isEmailAddressTaken("e5@test.com"));
    assertFalse(repository.isEmailAddressTaken(null));
  }

  @Test
  public void findTemporaryAdmin() {
    // prepare
    TemporaryAdmin tmp1 = inTransaction(() -> repository.createTemporary(new TemporaryAdmin().setAccessIdHash("hash1").setEmailAddress("").setInvalid(false).setExpirationTime(Instant.now().plusSeconds(600))));
    TemporaryAdmin tmp2 = inTransaction(() -> repository.createTemporary(new TemporaryAdmin().setAccessIdHash("hash2").setEmailAddress("").setInvalid(true).setExpirationTime(Instant.now().plusSeconds(600))));
    TemporaryAdmin tmp3 = inTransaction(() -> repository.createTemporary(new TemporaryAdmin().setAccessIdHash("hash3").setEmailAddress("").setInvalid(false).setExpirationTime(Instant.now().minusSeconds(600))));

    // execute & verify
    Optional<TemporaryAdmin> result = repository.findTemporaryAdmin("hash1");
    assertThat(result.get().getId()).isEqualTo(tmp1.getId());

    // execute & verify
    result = repository.findTemporaryAdmin("hash2");
    assertFalse(result.isPresent());

    // execute & verify
    result = repository.findTemporaryAdmin("hash3");
    assertFalse(result.isPresent());
  }

  @Test
  public void findTemporaryAdminEmailAddress() {
    // prepare
    TemporaryAdminEmailAddress tmp1 = inTransaction(() -> repository.createTemporaryEmail(new TemporaryAdminEmailAddress().setAccessIdHash("hash1").setEmailAddress("").setInvalid(false).setExpirationTime(Instant.now().plusSeconds(600))));
    TemporaryAdminEmailAddress tmp2 = inTransaction(() -> repository.createTemporaryEmail(new TemporaryAdminEmailAddress().setAccessIdHash("hash2").setEmailAddress("").setInvalid(true).setExpirationTime(Instant.now().plusSeconds(600))));
    TemporaryAdminEmailAddress tmp3 = inTransaction(() -> repository.createTemporaryEmail(new TemporaryAdminEmailAddress().setAccessIdHash("hash3").setEmailAddress("").setInvalid(false).setExpirationTime(Instant.now().minusSeconds(600))));

    // execute & verify
    Optional<TemporaryAdminEmailAddress> result = repository.findTemporaryAdminEmailAddress("hash1");
    assertThat(result.get().getId()).isEqualTo(tmp1.getId());

    // execute & verify
    result = repository.findTemporaryAdminEmailAddress("hash2");
    assertFalse(result.isPresent());

    // execute & verify
    result = repository.findTemporaryAdminEmailAddress("hash3");
    assertFalse(result.isPresent());
  }

  @Test
  public void create() {
    // create
    Community com = inTransaction(() -> communityRepository.create(new Community().setName("com")));
    Admin adm = inTransaction(() -> repository.create(
        new Admin()
          .setEmailAddress("a@a.com")
          .setRole(NCL)
          .setCommunity(new Community().setId(com.getId()))));

    // find
    Admin result = em().find(Admin.class, adm.getId());
    
    // verify
    assertThat(result.getId()).isEqualTo(adm.getId());
    assertThat(result.getRole().getRolename()).isEqualTo("NCL運営者");
    assertThat(result.getCommunity().getName()).isEqualTo("com");
  }
  
  @Test
  public void createTemporary() {
    // create
    Community com = inTransaction(() -> communityRepository.create(new Community().setName("com")));
    TemporaryAdmin tmp = inTransaction(() -> repository.createTemporary(
        new TemporaryAdmin()
          .setAccessIdHash("accessIdHash")
          .setExpirationTime(Instant.now())
          .setInvalid(false)
          .setEmailAddress("a@a.com")
          .setRole(COMMUNITY_ADMIN)
          .setCommunity(new Community().setId(com.getId()))));

    // find
    TemporaryAdmin result = em().find(TemporaryAdmin.class, tmp.getId());
    
    // verify
    assertThat(result.getId()).isEqualTo(tmp.getId());
    assertThat(result.getRole().getRolename()).isEqualTo("コミュニティ管理者");
    assertThat(result.getCommunity().getName()).isEqualTo("com");
  }
  
  @Test
  public void createTemporaryEmail() {
    // create
    TemporaryAdminEmailAddress tmp = inTransaction(() -> repository.createTemporaryEmail(
        new TemporaryAdminEmailAddress()
          .setAccessIdHash("accessIdHash")
          .setExpirationTime(Instant.now())
          .setInvalid(false)
          .setEmailAddress("a@a.com")));

    // find
    TemporaryAdminEmailAddress result = em().find(TemporaryAdminEmailAddress.class, tmp.getId());
    
    // verify
    assertThat(result.getEmailAddress()).isEqualTo("a@a.com");
  }
  
  @Test
  public void update() {
    // create
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    Admin adm1 = inTransaction(() -> repository.create(new Admin().setEmailAddress("a@a.com").setRole(NCL).setCommunity(com1)));

    // update
    Admin adm2 = inTransaction(() -> repository.update(adm1.setAdminname("adm2").setRole(TELLER).setCommunity(new Community().setId(com2.getId()))));
    
    // find
    Admin result = em().find(Admin.class, adm2.getId());
    
    // verify
    assertThat(result.getAdminname()).isEqualTo("adm2");
    assertThat(result.getRole().getRolename()).isEqualTo("窓口担当者");
    assertThat(result.getCommunity().getName()).isEqualTo("com2");
  }
  
  @Test
  public void updateTemporaryAdmin() {
    // create
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    TemporaryAdmin tmp1 = inTransaction(() -> repository.createTemporary(new TemporaryAdmin().setAccessIdHash("").setInvalid(false).setExpirationTime(Instant.now()).setEmailAddress("").setRole(NCL).setCommunity(com1)));

    // update
    TemporaryAdmin tmp2 = inTransaction(() -> repository.updateTemporaryAdmin(tmp1.setInvalid(true).setRole(TELLER).setCommunity(new Community().setId(com2.getId()))));
    
    // find
    TemporaryAdmin result = em().find(TemporaryAdmin.class, tmp2.getId());
    
    // verify
    assertThat(result.isInvalid()).isEqualTo(true);
    assertThat(result.getRole().getRolename()).isEqualTo("窓口担当者");
    assertThat(result.getCommunity().getName()).isEqualTo("com2");
  }
  
  @Test
  public void updateTemporaryEmail() {
    // create
    TemporaryAdminEmailAddress tmp1 = inTransaction(() -> repository.createTemporaryEmail(new TemporaryAdminEmailAddress().setAccessIdHash("").setExpirationTime(Instant.now()).setInvalid(false).setEmailAddress("")));
    // update
    TemporaryAdminEmailAddress tmp2 = inTransaction(() -> repository.updateTemporaryEmail(tmp1.setInvalid(true)));
    
    // find
    TemporaryAdminEmailAddress result = em().find(TemporaryAdminEmailAddress.class, tmp2.getId());
    
    // verify
    assertThat(result.isInvalid()).isEqualTo(true);
  }
}