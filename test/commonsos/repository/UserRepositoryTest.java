package commonsos.repository;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import commonsos.repository.CommunityRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class UserRepositoryTest extends DBTest {

  private UserRepository repository = new UserRepository(entityManagerService);
  private CommunityRepository communityRepository = new CommunityRepository(entityManagerService);

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
  public void update() {
    // prepare
    User testUser = createTestUser_BelongAtOneCommunity();
    
    // execute
    testUser.setFirstName("new first name")
        .setLastName("new last name")
        .setDescription("new description")
        .setLocation("new location")
        .setEmailAddress("new@test.com");
    repository.update(testUser);

    // verify
    User updatedUser = em().find(User.class, testUser.getId());
    assertUser(updatedUser, testUser);
  }

  @Test
  public void search() {
    // prepare
    User admin = inTransaction(() -> repository.create(new User().setUsername("fooUser")));
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1").setAdminUser(admin)));
    Community community2 = inTransaction(() -> communityRepository.create(new Community().setName("community2")));
    Community community3 = inTransaction(() -> communityRepository.create(new Community().setName("community3")));
    Community community4 = inTransaction(() -> communityRepository.create(new Community().setName("community4")));
    inTransaction(() -> repository.update(admin.setCommunityList(asList(community1, community2))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("foobarUser").setCommunityList(asList(community2, community3))));
    inTransaction(() -> repository.create(new User().setUsername("hogeUser").setCommunityList(asList(community2))));
    
    // execute
    List<User> results = repository.search(community1.getId(), "foo");
    
    // verify
    assertThat(results.size()).isEqualTo(1);
    assertThat(results.get(0).getUsername()).isEqualTo("fooUser");

    // execute
    results = repository.search(community2.getId(), "foo");
    
    // verify
    results.sort((a,b) -> a.getId().compareTo(b.getId()));
    results.get(0).getCommunityList().sort((a,b) -> a.getId().compareTo(b.getId()));
    assertThat(results.size()).isEqualTo(2);
    assertThat(results.get(0).getUsername()).isEqualTo("fooUser");
    assertThat(results.get(0).getCommunityList().get(0).getName()).isEqualTo("community1");
    assertThat(results.get(0).getCommunityList().get(0).getAdminUser().getUsername()).isEqualTo("fooUser");
    assertThat(results.get(0).getCommunityList().get(0).getAdminUser().getCommunityList().get(0).getName()).isEqualTo("community1");
    assertThat(results.get(0).getCommunityList().get(1).getName()).isEqualTo("community2");
    assertThat(results.get(0).getCommunityList().get(1).getAdminUser()).isNull();
    assertThat(results.get(1).getUsername()).isEqualTo("foobarUser");

    // execute
    results = repository.search(community3.getId(), "foo");
    
    // verify
    assertThat(results.size()).isEqualTo(1);
    assertThat(results.get(0).getUsername()).isEqualTo("foobarUser");

    // execute
    results = repository.search(community4.getId(), "foo");
    
    // verify
    assertThat(results.size()).isEqualTo(0);
  }

  @Test
  public void search_deleted() {
    // prepare
    Long community1Id = inTransaction(() -> communityRepository.create(new Community().setName("community1"))).getId();
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityList(asList(new Community().setId(community1Id)))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityList(asList(new Community().setId(community1Id))).setDeleted(true)));
    
    // execute
    List<User> results = repository.search(community1Id, "user");

    // verify
    assertThat(results.size()).isEqualTo(1);
    assertThat(results.get(0).getUsername()).isEqualTo("fooUser");
  }

  @Test
  public void search_emptyQuery() {
    // prepare
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")));
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityList(asList(community1))));
    
    // execute
    List<User> results = repository.search(community1.getId(), null);
    
    // verify
    assertThat(results.size()).isEqualTo(0);
    
    // execute
    results = repository.search(id("community1"), "");

    // verify
    assertThat(results.size()).isEqualTo(0);
  }

  @Test
  public void search_notFound() {
    // prepare
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")));
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityList(asList(community1))));
    
    // execute
    List<User> results = repository.search(community1.getId(), "hogehoge");
    
    // verify
    assertThat(results.size()).isEqualTo(0);
  }

  @Test
  public void search_maxResults() {
    // prepare
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")));
    inTransaction(() -> repository.create(new User().setUsername("user1").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user2").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user3").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user4").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user5").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user6").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user7").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user8").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user9").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user10").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user11").setCommunityList(asList(community1))));
    inTransaction(() -> repository.create(new User().setUsername("user12").setCommunityList(asList(community1))));
    
    // execute
    List<User> results = repository.search(community1.getId(), "user");
    
    // verify
    assertThat(results.size()).isEqualTo(10);
  }

  private User createTestUser_BelongAtZeroCommunity() {
    User testUser =  new User()
        .setCommunityList(asList())
        .setUsername("worker")
        .setPasswordHash("password hash")
        .setFirstName("first name")
        .setLastName("last name")
        .setDescription("description")
        .setLocation("location")
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
        .setCommunityList(asList(community1))
        .setUsername("worker")
        .setPasswordHash("password hash")
        .setFirstName("first name")
        .setLastName("last name")
        .setDescription("description")
        .setLocation("location")
        .setAvatarUrl("avatar url")
        .setWallet("wallet")
        .setWalletAddress("wallet address")
        .setPushNotificationToken("push notification token")
        .setEmailAddress("test@test.com");
    
    return inTransaction(() -> repository.create(testUser));
  }

  private User createTestUser_BelongAtTwoCommunity() {
    Community community1 = new Community().setName("community1").setTokenContractAddress("0x1");
    communityRepository.create(community1);
    Community community2 = new Community().setName("community2").setTokenContractAddress("0x2");
    communityRepository.create(community2);

    User testUser =  new User()
        .setCommunityList(asList(community1, community2))
        .setUsername("worker")
        .setPasswordHash("password hash")
        .setFirstName("first name")
        .setLastName("last name")
        .setDescription("description")
        .setLocation("location")
        .setAvatarUrl("avatar url")
        .setWallet("wallet")
        .setWalletAddress("wallet address")
        .setPushNotificationToken("push notification token")
        .setEmailAddress("test@test.com");
    
    return inTransaction(() -> repository.create(testUser));
  }
  
  private void assertUser(User actual, User expect) {
    assertThat(actual.getId()).isEqualTo(expect.getId());
    assertThat(actual.getUsername()).isEqualTo(expect.getUsername());
    assertThat(actual.getPasswordHash()).isEqualTo(expect.getPasswordHash());
    assertThat(actual.getFirstName()).isEqualTo(expect.getFirstName());
    assertThat(actual.getLastName()).isEqualTo(expect.getLastName());
    assertThat(actual.getDescription()).isEqualTo(expect.getDescription());
    assertThat(actual.getLocation()).isEqualTo(expect.getLocation());
    assertThat(actual.getAvatarUrl()).isEqualTo(expect.getAvatarUrl());
    assertThat(actual.getWallet()).isEqualTo(expect.getWallet());
    assertThat(actual.getWalletAddress()).isEqualTo(expect.getWalletAddress());
    assertThat(actual.getPushNotificationToken()).isEqualTo(expect.getPushNotificationToken());
    assertThat(actual.getEmailAddress()).isEqualTo(expect.getEmailAddress());
    assertThat(actual.isDeleted()).isEqualTo(expect.isDeleted());

    assertThat(actual.getCommunityList().size()).isEqualTo(expect.getCommunityList().size());
    actual.getCommunityList().sort((a,b) -> a.getId().compareTo(b.getId()));
    expect.getCommunityList().sort((a,b) -> a.getId().compareTo(b.getId()));
    for (int i = 0; i < actual.getCommunityList().size(); i++) {
      assertThat(actual.getCommunityList().get(i).getId()).isEqualTo(expect.getCommunityList().get(i).getId());
      assertThat(actual.getCommunityList().get(i).getName()).isEqualTo(expect.getCommunityList().get(i).getName());
      assertThat(actual.getCommunityList().get(i).getTokenContractAddress()).isEqualTo(expect.getCommunityList().get(i).getTokenContractAddress());
    }
  }
}