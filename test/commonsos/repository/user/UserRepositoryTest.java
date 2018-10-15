package commonsos.repository.user;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import commonsos.DBTest;

public class UserRepositoryTest extends DBTest {

  private UserRepository repository = new UserRepository(entityManagerService);

  @Test
  public void findByUsername() {
    // prepare
    User testUser = createTestUser();

    // execute
    Optional<User> result = repository.findByUsername(testUser.getUsername());
    
    // verify
    assertThat(result.isPresent());
    assertUser(result.get(), testUser);
  }

  @Test
  public void findByUsername_deleted() {
    // prepare
    User testUser = createTestUser();
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
  public void create() {
    // execute
    User testUser = createTestUser();
    
    // verify
    User createdUser = em().find(User.class, testUser.getId());
    assertUser(createdUser, testUser);
  }

  @Test
  public void findById() {
    // prepare
    User testUser = createTestUser();
    
    // execute
    Optional<User> result = repository.findById(testUser.getId());

    // verify
    assertThat(result.isPresent());
    assertUser(result.get(), testUser);
  }

  @Test
  public void findById_deleted() {
    // prepare
    User testUser = createTestUser();
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
    User testUser = createTestUser();
    
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
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("foobarUser").setCommunityId(id("community2"))));
    inTransaction(() -> repository.create(new User().setUsername("hogeUser").setCommunityId(id("community2"))));
    
    // execute
    List<User> results = repository.search(id("community1"), "foo");
    
    // verify
    assertThat(results.size()).isEqualTo(1);
    assertThat(results.get(0).getUsername()).isEqualTo("fooUser");
    
    // execute
    results = repository.search(id("community2"), "user");

    // verify
    results.sort((a,b) -> a.getId().compareTo(b.getId()));
    assertThat(results.size()).isEqualTo(2);
    assertThat(results.get(0).getUsername()).isEqualTo("foobarUser");
    assertThat(results.get(1).getUsername()).isEqualTo("hogeUser");
  }

  @Test
  public void search_deleted() {
    // prepare
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityId(id("community1")).setDeleted(true)));
    
    // execute
    List<User> results = repository.search(id("community1"), "user");

    // verify
    assertThat(results.size()).isEqualTo(1);
    assertThat(results.get(0).getUsername()).isEqualTo("fooUser");
  }

  @Test
  public void search_emptyQuery() {
    // prepare
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityId(id("community1"))));
    
    // execute
    List<User> results = repository.search(id("community1"), null);
    
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
    inTransaction(() -> repository.create(new User().setUsername("fooUser").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("barUser").setCommunityId(id("community1"))));
    
    // execute
    List<User> results = repository.search(id("community1"), "hogehoge");
    
    // verify
    assertThat(results.size()).isEqualTo(0);
  }

  @Test
  public void search_maxResults() {
    // prepare
    inTransaction(() -> repository.create(new User().setUsername("user1").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user2").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user3").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user4").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user5").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user6").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user7").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user8").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user9").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user10").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user11").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new User().setUsername("user12").setCommunityId(id("community1"))));
    
    // execute
    List<User> results = repository.search(id("community1"), "user");
    
    // verify
    assertThat(results.size()).isEqualTo(10);
  }

  private User createTestUser() {
    User testUser =  new User()
        .setCommunityId(id("community id"))
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
    assertThat(actual.getCommunityId()).isEqualTo(expect.getCommunityId());
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
  }
}