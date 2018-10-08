package commonsos.domain.auth;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;

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
    Long id = inTransaction(() -> repository.create(new User().setUsername("worker")).getId());

    assertThat(repository.findById(id)).isNotEmpty();
  }

  @Test
  public void findById_notFound() {
    assertThat(repository.findById(id("invalid id"))).isEmpty();
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
    User user1 = inTransaction(() -> repository.create(new User().setFirstName("FIRST").setLastName("foo").setCommunityId(id("community"))));
    User user2 = inTransaction(() -> repository.create(new User().setFirstName("first").setLastName("bar").setCommunityId(id("community"))));

    assertThat(repository.search(id("community"), "irs")).containsExactly(user1, user2);
    assertThat(repository.search(id("community"), "FOO")).containsExactly(user1);
    assertThat(repository.search(id("community"), "baz")).isEmpty();
    assertThat(repository.search(id("community"), " ")).isEmpty();
    assertThat(repository.search(id("community"), "")).isEmpty();
  }

  @Test
  public void search_excludesOtherCommunities() {
    User user1 = inTransaction(() -> repository.create(new User().setFirstName("first").setLastName("foo").setCommunityId(id("Shibuya"))));
    User user2 = inTransaction(() -> repository.create(new User().setFirstName("first").setLastName("bar").setCommunityId(id("Kaga"))));

    assertThat(repository.search(id("Shibuya"), "first")).containsExactly(user1);
    assertThat(repository.search(id("Kaga"), "first")).containsExactly(user2);
  }

  @Test
  public void search_includesAdminUser() {
    User admin = inTransaction(() -> repository.create(new User().setCommunityId(id("community")).setFirstName("name").setLastName("name").setAdmin(true)));
    User user = inTransaction(() -> repository.create(new User().setCommunityId(id("community")).setFirstName("name").setLastName("name").setAdmin(false)));

    assertThat(repository.search(id("community"), "name")).containsExactly(admin, user);
  }

  @Test
  public void findAdminByCommunityId() {
    inTransaction(() -> repository.create(new User().setCommunityId(id("community"))));
    User admin = inTransaction(() -> repository.create(new User().setCommunityId(id("community")).setAdmin(true)));

    User result = repository.findAdminByCommunityId(id("community"));

    assertThat(result).isEqualTo(admin);
  }
  
  private User createTestUser() {
    User testUser =  new User()
        .setCommunityId(id("community id"))
        .setAdmin(false)
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
    assertThat(actual.isAdmin()).isEqualTo(expect.isAdmin());
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
  }
}