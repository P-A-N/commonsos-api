package commonsos.repository;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.PasswordResetRequest;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.TemporaryEmailAddress;
import commonsos.repository.entity.TemporaryUser;
import commonsos.repository.entity.User;
import commonsos.service.command.PaginationCommand;

public class UserRepositoryTest extends AbstractRepositoryTest {

  private UserRepository repository = new UserRepository(emService);
  private CommunityRepository communityRepository = new CommunityRepository(emService);

  @Test
  public void findByUsername() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();

    // execute
    Optional<User> result = repository.findByUsername(testUser.getUsername());
    
    // verify
    assertThat(result.isPresent());
    assertUser(result.get(), testUser);
  }

  @Test
  public void findByUsername_deleted() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();
    testUser.setDeleted(true);
    inTransaction(() -> repository.update(testUser));

    // execute
    Optional<User> result = repository.findByUsername(testUser.getUsername());

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findByUsername_notFound() {
    // execute
    Optional<User> result = repository.findByUsername("worker");
    
    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findByEmailAddress() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();

    // execute
    Optional<User> result = repository.findByEmailAddress(testUser.getEmailAddress());
    
    // verify
    assertThat(result.isPresent());
    assertUser(result.get(), testUser);
  }

  @Test
  public void findByEmailAddress_deleted() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();
    inTransaction(() -> repository.update(testUser.setDeleted(true)));

    // execute
    Optional<User> result = repository.findByEmailAddress(testUser.getEmailAddress());

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findByEmailAddress_notFound() {
    // execute
    Optional<User> result = repository.findByEmailAddress("not@exists.address");
    
    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void isUsernameTaken() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();
    TemporaryUser temporaryUser = createTemporaryUser_BelongAtOneCommunity("temporaryUser");
    TemporaryUser invalidTemporaryUser = createTemporaryUser_BelongAtOneCommunity("invalidTemporaryUser");
    inTransaction(() -> repository.updateTemporary(invalidTemporaryUser.setUsername("itu").setInvalid(true)));
    TemporaryUser expiredTemporaryUser = createTemporaryUser_BelongAtOneCommunity("expirationTemporaryUser");
    inTransaction(() -> repository.updateTemporary(expiredTemporaryUser.setUsername("etu").setExpirationTime(Instant.now().minusSeconds(60))));
    

    // execute
    boolean result = repository.isUsernameTaken(testUser.getUsername());
    // verify
    assertThat(result).isTrue();

    // execute
    result = repository.isUsernameTaken(temporaryUser.getUsername());
    // verify
    assertThat(result).isTrue();

    // execute
    result = repository.isUsernameTaken("newUser");
    // verify
    assertThat(result).isFalse();

    // execute
    result = repository.isUsernameTaken(invalidTemporaryUser.getUsername());
    // verify
    assertThat(result).isFalse();

    // execute
    result = repository.isUsernameTaken(expiredTemporaryUser.getUsername());
    // verify
    assertThat(result).isFalse();
  }

  @Test
  public void isEmailAddressTaken() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();
    
    TemporaryUser temporaryUser = createTemporaryUser_BelongAtOneCommunity("temporaryUser");
    TemporaryUser invalidTemporaryUser = createTemporaryUser_BelongAtOneCommunity("invalidTemporaryUser");
    inTransaction(() -> repository.updateTemporary(invalidTemporaryUser.setEmailAddress("itu@test.com").setInvalid(true)));
    TemporaryUser expiredTemporaryUser = createTemporaryUser_BelongAtOneCommunity("expirationTemporaryUser");
    inTransaction(() -> repository.updateTemporary(expiredTemporaryUser.setEmailAddress("etu@test.com").setExpirationTime(Instant.now().minusSeconds(60))));

    TemporaryEmailAddress tempEmailAddress = createTemporaryEmailAddress("tempEmailAddress");
    TemporaryEmailAddress invalidTempEmailAddress = createTemporaryEmailAddress("invalidTempEmailAddress");
    inTransaction(() -> repository.updateTemporaryEmailAddress(invalidTempEmailAddress.setEmailAddress("itea@test.com").setInvalid(true)));
    TemporaryEmailAddress expiredTempEmailAddress = createTemporaryEmailAddress("expiredTempEmailAddress");
    inTransaction(() -> repository.updateTemporaryEmailAddress(expiredTempEmailAddress.setEmailAddress("etea@test.com").setExpirationTime(Instant.now().minusSeconds(60))));

    // execute
    boolean result = repository.isEmailAddressTaken(testUser.getEmailAddress());
    // verify
    assertThat(result).isTrue();

    // execute
    result = repository.isEmailAddressTaken(temporaryUser.getEmailAddress());
    // verify
    assertThat(result).isTrue();

    // execute
    result = repository.isEmailAddressTaken("notexists@text.com");
    // verify
    assertThat(result).isFalse();

    // execute
    result = repository.isEmailAddressTaken(invalidTemporaryUser.getEmailAddress());
    // verify
    assertThat(result).isFalse();

    // execute
    result = repository.isEmailAddressTaken(expiredTemporaryUser.getEmailAddress());
    // verify
    assertThat(result).isFalse();

    // execute
    result = repository.isEmailAddressTaken(tempEmailAddress.getEmailAddress());
    // verify
    assertThat(result).isTrue();

    // execute
    result = repository.isEmailAddressTaken(invalidTempEmailAddress.getEmailAddress());
    // verify
    assertThat(result).isFalse();

    // execute
    result = repository.isEmailAddressTaken(expiredTempEmailAddress.getEmailAddress());
    // verify
    assertThat(result).isFalse();
  }

  @Test
  public void create_BelongAtZeroCommunity() {
    // execute
    User testUser = createTestUser_BelongAtZeroCommunity();
    
    // verify
    User createdUser = em().find(User.class, testUser.getId());
    assertUser(createdUser, testUser);
  }

  @Test
  public void create_BelongAtOneCommunity() {
    // execute
    User testUser = createTestUser_BelongAtOneCommunity();
    
    // verify
    User createdUser = em().find(User.class, testUser.getId());
    assertUser(createdUser, testUser);
  }

  @Test
  public void create_BelongAtTwoCommunity() {
    // execute
    User testUser = createTestUser_BelongAtTwoCommunity();
    
    // verify
    User createdUser = em().find(User.class, testUser.getId());
    assertUser(createdUser, testUser);
  }

  @Test
  public void createTemporary_BelongAtZeroCommunity() {
    // execute
    TemporaryUser tempUser = createTemporaryUser_BelongAtZeroCommunity("tempUser");
    
    // verify
    TemporaryUser createdTempUser = em().find(TemporaryUser.class, tempUser.getId());
    assertTempUser(createdTempUser, tempUser);
  }

  @Test
  public void createTemporary_BelongAtOneCommunity() {
    // execute
    TemporaryUser tempUser = createTemporaryUser_BelongAtOneCommunity("tempUser");
    
    // verify
    TemporaryUser createdTempUser = em().find(TemporaryUser.class, tempUser.getId());
    assertTempUser(createdTempUser, tempUser);
  }

  @Test
  public void createTemporary_BelongAtTwoCommunity() {
    // execute
    TemporaryUser tempUser = createTemporaryUser_BelongAtTwoCommunity("tempUser");
    
    // verify
    TemporaryUser createdTempUser = em().find(TemporaryUser.class, tempUser.getId());
    assertTempUser(createdTempUser, tempUser);
  }

  @Test
  public void findById() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();
    
    // execute
    Optional<User> result = repository.findById(testUser.getId());

    // verify
    assertThat(result.isPresent());
    assertUser(result.get(), testUser);
  }

  @Test
  public void findById_deleted() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();
    testUser.setDeleted(true);
    inTransaction(() -> repository.update(testUser));

    // execute
    Optional<User> result = repository.findById(testUser.getId());

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findById_notFound() {
    // execute
    Optional<User> result = repository.findById(id("invalid id"));
    
    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryUser() {
    // prepare
    TemporaryUser tmpUser = createTemporaryUser_BelongAtOneCommunity("tmpUser");
    
    // execute
    Optional<TemporaryUser> result = repository.findTemporaryUser(tmpUser.getAccessIdHash());

    // verify
    assertThat(result.isPresent());
    assertTempUser(result.get(), tmpUser);
  }

  @Test
  public void findTemporaryUser_caseSensitive() {
    // prepare
    createTemporaryUser_BelongAtOneCommunity("tmpUser");
    
    // execute
    Optional<TemporaryUser> result = repository.findTemporaryUser("tmpuser");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryUser_isInvalid() {
    // prepare
    TemporaryUser tmpUser = createTemporaryUser_BelongAtOneCommunity("tmpUser");
    inTransaction(() -> repository.updateTemporary(tmpUser.setInvalid(true)));
    
    // execute
    Optional<TemporaryUser> result = repository.findTemporaryUser(tmpUser.getAccessIdHash());

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryUser_isExpired() {
    // prepare
    TemporaryUser tmpUser = createTemporaryUser_BelongAtOneCommunity("tmpUser");
    inTransaction(() -> repository.updateTemporary(tmpUser.setExpirationTime(Instant.now().minusSeconds(60))));
    
    // execute
    Optional<TemporaryUser> result = repository.findTemporaryUser(tmpUser.getAccessIdHash());

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryUser_notFound() {
    // execute
    Optional<TemporaryUser> result = repository.findTemporaryUser("invalid id");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryEmailAddress() {
    // prepare
    TemporaryEmailAddress tmpEmailAddr = createTemporaryEmailAddress("accessIdHash");
    
    // execute
    Optional<TemporaryEmailAddress> result = repository.findTemporaryEmailAddress("accessIdHash");

    // verify
    assertThat(result.isPresent());
    assertThat(result.get().getEmailAddress()).isEqualTo(tmpEmailAddr.getEmailAddress());
  }

  @Test
  public void findTemporaryEmailAddress_caseSensitive() {
    // prepare
    createTemporaryEmailAddress("accessIdHash");
    
    // execute
    Optional<TemporaryEmailAddress> result = repository.findTemporaryEmailAddress("accessidhash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryEmailAddress_isInvalid() {
    // prepare
    TemporaryEmailAddress tmpEmailAddr = createTemporaryEmailAddress("accessIdHash");
    inTransaction(() -> repository.updateTemporaryEmailAddress(tmpEmailAddr.setInvalid(true)));
    
    // execute
    Optional<TemporaryEmailAddress> result = repository.findTemporaryEmailAddress("accessIdHash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryEmailAddress_isExpired() {
    // prepare
    TemporaryEmailAddress tmpEmailAddr = createTemporaryEmailAddress("accessIdHash");
    inTransaction(() -> repository.updateTemporaryEmailAddress(tmpEmailAddr.setExpirationTime(Instant.now().minusSeconds(60))));
    
    // execute
    Optional<TemporaryEmailAddress> result = repository.findTemporaryEmailAddress("accessIdHash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findTemporaryEmailAddress_notFound() {
    // execute
    Optional<TemporaryEmailAddress> result = repository.findTemporaryEmailAddress("accessIdHash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findPasswordResetRequest() {
    // prepare
    PasswordResetRequest passReset = createPasswordResetRequest("accessIdHash");
    
    // execute
    Optional<PasswordResetRequest> result = repository.findPasswordResetRequest("accessIdHash");

    // verify
    assertThat(result.isPresent());
    assertThat(result.get().getUserId()).isEqualTo(passReset.getUserId());
  }

  @Test
  public void findPasswordResetRequest_caseSensitive() {
    // prepare
    createPasswordResetRequest("accessIdHash");
    
    // execute
    Optional<PasswordResetRequest> result = repository.findPasswordResetRequest("accessidhash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findPasswordResetRequest_isInvalid() {
    // prepare
    PasswordResetRequest passReset = createPasswordResetRequest("accessIdHash");
    inTransaction(() -> repository.updatePasswordResetRequest(passReset.setInvalid(true)));
    
    // execute
    Optional<PasswordResetRequest> result = repository.findPasswordResetRequest("accessIdHash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findPasswordResetRequest_isExpired() {
    // prepare
    PasswordResetRequest passReset = createPasswordResetRequest("accessIdHash");
    inTransaction(() -> repository.updatePasswordResetRequest(passReset.setExpirationTime(Instant.now().minusSeconds(60))));
    
    // execute
    Optional<PasswordResetRequest> result = repository.findPasswordResetRequest("accessIdHash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void findPasswordResetRequest_notFound() {
    // execute
    Optional<PasswordResetRequest> result = repository.findPasswordResetRequest("accessIdHash");

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void update() {
    // prepare
    Long userId = createTestUser_BelongAtOneCommunity().getId();
    
    // execute
    inTransaction(() -> {
      User target = repository.findStrictById(userId);
      target.setFirstName("new first name")
        .setLastName("new last name").setDescription("new description")
        .setLocation("new location")
        .setEmailAddress("new@test.com");
      repository.update(target);
    });

    // verify
    User updatedUser = em().find(User.class, userId);
    assertThat(updatedUser.getFirstName()).isEqualTo("new first name");
  }

  @Disabled
  @Test
  public void transactionTest() throws Exception {
    // prepare
    Long userId = createTestUser_BelongAtOneCommunity().getId();
    
    // execute
    Runnable r = () -> {
      inTransaction(() -> {
        User target = repository.findStrictById(userId);
        Integer num = NumberUtils.isParsable(target.getFirstName()) ? Integer.parseInt(target.getFirstName()) : 0;
        System.out.println(num);
        target.setFirstName(String.valueOf(num + 1));
        repository.update(target);
      });
    };
    ExecutorService es = Executors.newFixedThreadPool(10);
    try {
      for (int i = 0; i < 100; i++) {
        es.execute(r);
      }
    } finally {
      es.shutdown();
      es.awaitTermination(1, TimeUnit.MINUTES);
    }

    // verify
    User updatedUser = em().find(User.class, userId);
    assertThat(updatedUser.getFirstName()).isEqualTo("100");
  }

  @Test
  public void search() {
    // prepare
    User admin = inTransaction(() -> repository.create(new User().setUsername("fooUser")));
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1").setAdminUser(admin)));
    Community community2 = inTransaction(() -> communityRepository.create(new Community().setName("community2")));
    Community community3 = inTransaction(() -> communityRepository.create(new Community().setName("community3")));
    Community community4 = inTransaction(() -> communityRepository.create(new Community().setName("community4")));
    inTransaction(() -> repository.update(admin.setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2)))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("foobarUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3)))));
    inTransaction(() -> repository.create(new User().setUsername("hogeUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community2)))));
    
    // execute
    ResultList<User> result = repository.search(community1.getId(), "foo", null);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("fooUser");

    // execute
    result = repository.search(community2.getId(), "foo", null);
    
    // verify
    result.getList().sort((a,b) -> a.getId().compareTo(b.getId()));
    result.getList().get(0).getCommunityUserList().sort((a,b) -> a.getId().compareTo(b.getId()));
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("fooUser");
    assertThat(result.getList().get(0).getCommunityUserList().get(0).getCommunity().getName()).isEqualTo("community1");
    assertThat(result.getList().get(0).getCommunityUserList().get(0).getCommunity().getAdminUser().getUsername()).isEqualTo("fooUser");
    assertThat(result.getList().get(0).getCommunityUserList().get(0).getCommunity().getAdminUser().getCommunityUserList().get(0).getCommunity().getName()).isEqualTo("community1");
    assertThat(result.getList().get(0).getCommunityUserList().get(1).getCommunity().getName()).isEqualTo("community2");
    assertThat(result.getList().get(0).getCommunityUserList().get(1).getCommunity().getAdminUser()).isNull();
    assertThat(result.getList().get(1).getUsername()).isEqualTo("foobarUser");

    // execute
    result = repository.search(community3.getId(), "foo", null);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("foobarUser");

    // execute
    result = repository.search(community4.getId(), "foo", null);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void search_deleted() {
    // prepare
    Long community1Id = inTransaction(() -> communityRepository.create(new Community().setName("community1"))).getId();
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(community1Id))))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setDeleted(true).setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(community1Id))))));
    
    // execute
    ResultList<User> result = repository.search(community1Id, "user", null);

    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("fooUser");
  }

  @Test
  public void search_emptyQuery() {
    // prepare
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")));
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1)))));
    
    // execute
    ResultList<User> result = repository.search(community1.getId(), null, null);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("fooUser");
    assertThat(result.getList().get(1).getUsername()).isEqualTo("barUser");
    
    // execute
    result = repository.search(community1.getId(), "", null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("fooUser");
    assertThat(result.getList().get(1).getUsername()).isEqualTo("barUser");
  }

  @Test
  public void search_notFound() {
    // prepare
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")));
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1)))));
    
    // execute
    ResultList<User> result = repository.search(community1.getId(), "hogehoge", null);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void search_pagination() {
    // prepare
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")));
    inTransaction(() -> repository.create(new User().setUsername("user1").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user2").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user3").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user4").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user5").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user6").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user7").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user8").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user9").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user10").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user11").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    inTransaction(() -> repository.create(new User().setUsername("user12").setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))));
    
    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(10).setSort(SortType.ASC);
    ResultList<User> result = repository.search(community1.getId(), "user", pagination);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(10);

    // execute
    pagination.setPage(1);
    result = repository.search(community1.getId(), "user", pagination);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  private User createTestUser_BelongAtZeroCommunity() {
    User testUser =  new User()
        .setCommunityUserList(asList())
        .setUsername("worker")
        .setPasswordHash("password hash")
        .setFirstName("first name")
        .setLastName("last name")
        .setDescription("description")
        .setLocation("location")
        .setStatus("status")
        .setAvatarUrl("avatar url")
        .setWallet("wallet")
        .setWalletAddress("wallet address")
        .setPushNotificationToken("push notification token")
        .setEmailAddress("test@test.com");
    
    return inTransaction(() -> repository.create(testUser));
  }

  private User createTestUser_BelongAtOneCommunity() {
    Community community1 = new Community().setName("community1").setTokenContractAddress("0x1");
    inTransaction(() -> communityRepository.create(community1));
    
    User testUser =  new User()
        .setCommunityUserList(asList(new CommunityUser().setCommunity(community1)))
        .setUsername("worker")
        .setPasswordHash("password hash")
        .setFirstName("first name")
        .setLastName("last name")
        .setDescription("description")
        .setLocation("location")
        .setStatus("status")
        .setAvatarUrl("avatar url")
        .setWallet("wallet")
        .setWalletAddress("wallet address")
        .setPushNotificationToken("push notification token")
        .setEmailAddress("test@test.com");
    
    return inTransaction(() -> repository.create(testUser));
  }

  private User createTestUser_BelongAtTwoCommunity() {
    Community community1 = new Community().setName("community1").setTokenContractAddress("0x1");
    inTransaction(() -> communityRepository.create(community1));
    Community community2 = new Community().setName("community2").setTokenContractAddress("0x2");
    inTransaction(() -> communityRepository.create(community2));

    User testUser =  new User()
        .setCommunityUserList(asList(
            new CommunityUser().setCommunity(community1),
            new CommunityUser().setCommunity(community2)))
        .setUsername("worker")
        .setPasswordHash("password hash")
        .setFirstName("first name")
        .setLastName("last name")
        .setDescription("description")
        .setLocation("location")
        .setStatus("status")
        .setAvatarUrl("avatar url")
        .setWallet("wallet")
        .setWalletAddress("wallet address")
        .setPushNotificationToken("push notification token")
        .setEmailAddress("test@test.com");
    
    return inTransaction(() -> repository.create(testUser));
  }

  private TemporaryUser createTemporaryUser_BelongAtZeroCommunity(String accessIdHash) {
    TemporaryUser testUser =  new TemporaryUser()
        .setAccessIdHash(accessIdHash)
        .setExpirationTime(Instant.now().plusSeconds(60))
        .setInvalid(false)
        .setDescription("description")
        .setFirstName("first name")
        .setLastName("last name")
        .setLocation("location")
        .setPasswordHash("password hash")
        .setUsername("worker")
        .setEmailAddress("test@test.com")
        .setCommunityList(asList());
    
    return inTransaction(() -> repository.createTemporary(testUser));
  }

  private TemporaryUser createTemporaryUser_BelongAtOneCommunity(String accessIdHash) {
    Community community1 = new Community().setName("community1").setTokenContractAddress("0x1");
    inTransaction(() -> communityRepository.create(community1));
    
    TemporaryUser testUser =  new TemporaryUser()
        .setAccessIdHash(accessIdHash)
        .setExpirationTime(Instant.now().plusSeconds(60))
        .setInvalid(false)
        .setDescription("description")
        .setFirstName("first name")
        .setLastName("last name")
        .setLocation("location")
        .setPasswordHash("password hash")
        .setUsername("worker")
        .setEmailAddress("test@test.com")
        .setCommunityList(asList(community1));
    
    return inTransaction(() -> repository.createTemporary(testUser));
  }

  private TemporaryUser createTemporaryUser_BelongAtTwoCommunity(String accessIdHash) {
    Community community1 = new Community().setName("community1").setTokenContractAddress("0x1");
    inTransaction(() -> communityRepository.create(community1));
    Community community2 = new Community().setName("ommunity2").setTokenContractAddress("0x2");
    inTransaction(() -> communityRepository.create(community2));
    
    TemporaryUser testUser =  new TemporaryUser()
        .setAccessIdHash(accessIdHash)
        .setExpirationTime(Instant.now().plusSeconds(60))
        .setInvalid(false)
        .setDescription("description")
        .setFirstName("first name")
        .setLastName("last name")
        .setLocation("location")
        .setPasswordHash("password hash")
        .setUsername("worker")
        .setEmailAddress("test@test.com")
        .setCommunityList(asList(community1, community2));
    
    return inTransaction(() -> repository.createTemporary(testUser));
  }
  
  private TemporaryEmailAddress createTemporaryEmailAddress(String accessIdHash) {
    TemporaryEmailAddress tmpEmailAddr = new TemporaryEmailAddress()
        .setAccessIdHash(accessIdHash)
        .setExpirationTime(Instant.now().plusSeconds(60))
        .setInvalid(false)
        .setUserId(id("user"))
        .setEmailAddress("test@test.com");
    return inTransaction(() -> repository.createTemporaryEmailAddress(tmpEmailAddr));
  }
  
  private PasswordResetRequest createPasswordResetRequest(String accessIdHash) {
    PasswordResetRequest passReset = new PasswordResetRequest()
        .setAccessIdHash(accessIdHash)
        .setExpirationTime(Instant.now().plusSeconds(60))
        .setInvalid(false)
        .setUserId(id("user"));
    return inTransaction(() -> repository.createPasswordResetRequest(passReset));
  }

  private void assertUser(User actual, User expect) {
    assertThat(actual.getId()).isEqualTo(expect.getId());
    assertThat(actual.getUsername()).isEqualTo(expect.getUsername());
    assertThat(actual.getPasswordHash()).isEqualTo(expect.getPasswordHash());
    assertThat(actual.getFirstName()).isEqualTo(expect.getFirstName());
    assertThat(actual.getLastName()).isEqualTo(expect.getLastName());
    assertThat(actual.getDescription()).isEqualTo(expect.getDescription());
    assertThat(actual.getLocation()).isEqualTo(expect.getLocation());
    assertThat(actual.getStatus()).isEqualTo(expect.getStatus());
    assertThat(actual.getAvatarUrl()).isEqualTo(expect.getAvatarUrl());
    assertThat(actual.getWallet()).isEqualTo(expect.getWallet());
    assertThat(actual.getWalletAddress()).isEqualTo(expect.getWalletAddress());
    assertThat(actual.getPushNotificationToken()).isEqualTo(expect.getPushNotificationToken());
    assertThat(actual.getEmailAddress()).isEqualTo(expect.getEmailAddress());
    assertThat(actual.isDeleted()).isEqualTo(expect.isDeleted());

    assertThat(actual.getCommunityUserList().size()).isEqualTo(expect.getCommunityUserList().size());
    actual.getCommunityUserList().sort((a,b) -> a.getId().compareTo(b.getId()));
    expect.getCommunityUserList().sort((a,b) -> a.getId().compareTo(b.getId()));
    for (int i = 0; i < actual.getCommunityUserList().size(); i++) {
      assertThat(actual.getCommunityUserList().get(i).getId()).isEqualTo(expect.getCommunityUserList().get(i).getId());
      assertThat(actual.getCommunityUserList().get(i).getWalletLastViewTime()).isEqualTo(expect.getCommunityUserList().get(i).getWalletLastViewTime());
      assertThat(actual.getCommunityUserList().get(i).getAdLastViewTime()).isEqualTo(expect.getCommunityUserList().get(i).getAdLastViewTime());
      assertThat(actual.getCommunityUserList().get(i).getNotificationLastViewTime()).isEqualTo(expect.getCommunityUserList().get(i).getNotificationLastViewTime());
      assertThat(actual.getCommunityUserList().get(i).getCommunity().getId()).isEqualTo(expect.getCommunityUserList().get(i).getCommunity().getId());
      assertThat(actual.getCommunityUserList().get(i).getCommunity().getName()).isEqualTo(expect.getCommunityUserList().get(i).getCommunity().getName());
      assertThat(actual.getCommunityUserList().get(i).getCommunity().getDescription()).isEqualTo(expect.getCommunityUserList().get(i).getCommunity().getDescription());
      assertThat(actual.getCommunityUserList().get(i).getCommunity().getTokenContractAddress()).isEqualTo(expect.getCommunityUserList().get(i).getCommunity().getTokenContractAddress());
    }
  }

  private void assertTempUser(TemporaryUser actual, TemporaryUser expect) {
    assertThat(actual.getAccessIdHash()).isEqualTo(expect.getAccessIdHash());
    assertThat(actual.isInvalid()).isEqualTo(expect.isInvalid());
    assertThat(actual.getExpirationTime()).isEqualTo(expect.getExpirationTime());
    assertThat(actual.getDescription()).isEqualTo(expect.getDescription());
    assertThat(actual.getFirstName()).isEqualTo(expect.getFirstName());
    assertThat(actual.getLastName()).isEqualTo(expect.getLastName());
    assertThat(actual.getLocation()).isEqualTo(expect.getLocation());
    assertThat(actual.getPasswordHash()).isEqualTo(expect.getPasswordHash());
    assertThat(actual.getUsername()).isEqualTo(expect.getUsername());
    assertThat(actual.getEmailAddress()).isEqualTo(expect.getEmailAddress());

    assertThat(actual.getCommunityList().size()).isEqualTo(expect.getCommunityList().size());
    actual.getCommunityList().sort((a,b) -> a.getId().compareTo(b.getId()));
    expect.getCommunityList().sort((a,b) -> a.getId().compareTo(b.getId()));
    for (int i = 0; i < actual.getCommunityList().size(); i++) {
      assertThat(actual.getCommunityList().get(i).getId()).isEqualTo(expect.getCommunityList().get(i).getId());
      assertThat(actual.getCommunityList().get(i).getName()).isEqualTo(expect.getCommunityList().get(i).getName());
      assertThat(actual.getCommunityList().get(i).getDescription()).isEqualTo(expect.getCommunityList().get(i).getDescription());
      assertThat(actual.getCommunityList().get(i).getTokenContractAddress()).isEqualTo(expect.getCommunityList().get(i).getTokenContractAddress());
    }
  }
}