package commonsos.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class CommunityRepositoryTest extends RepositoryTest {

  UserRepository userRepository = new UserRepository(emService);
  CommunityRepository repository = new CommunityRepository(emService);

  @Test
  public void findById() {
    // prepare
    User admin = inTransaction(() -> userRepository.create(new User().setUsername("admin")));
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setDescription("description")
        .setTokenContractAddress("66")
        .setAdminUser(admin))).getId();

    // execute
    Optional<Community> community = repository.findById(id);

    // verify
    assertThat(community).isNotEmpty();
    assertThat(community.get().getId()).isEqualTo(id);
    assertThat(community.get().getName()).isEqualTo("Kaga");
    assertThat(community.get().getDescription()).isEqualTo("description");
    assertThat(community.get().getTokenContractAddress()).isEqualTo("66");
    assertThat(community.get().getAdminUser().getId()).isEqualTo(admin.getId());
    assertThat(community.get().getAdminUser().getUsername()).isEqualTo("admin");
  }

  @Test
  public void findById_noAdmin() {
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setDescription("description")
        .setTokenContractAddress("66"))).getId();

    Optional<Community> community = repository.findById(id);

    assertThat(community).isNotEmpty();
    assertThat(community.get().getId()).isEqualTo(id);
    assertThat(community.get().getName()).isEqualTo("Kaga");
    assertThat(community.get().getDescription()).isEqualTo("description");
    assertThat(community.get().getTokenContractAddress()).isEqualTo("66");
    assertThat(community.get().getAdminUser()).isNull();
  }

  @Test
  public void findById_notFound() {
    assertThat(repository.findById(123L)).isEmpty();
  }

  @Test
  public void list() {
    Long adminId = inTransaction(() -> userRepository.create(new User().setUsername("admin"))).getId();
    inTransaction(() -> {
      em().persist(new Community().setName("community1").setDescription("des1").setTokenContractAddress("66").setAdminUser(new User().setId(adminId)));
      em().persist(new Community().setName("community2").setDescription("des2").setTokenContractAddress("66"));
      em().persist(new Community().setName("community3").setDescription("des3").setTokenContractAddress(null));
    });

    List<Community> result = repository.list();
    result.sort((a,b) -> a.getId().compareTo(b.getId()));

    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getName()).isEqualTo("community1");
    assertThat(result.get(0).getDescription()).isEqualTo("des1");
    assertThat(result.get(0).getAdminUser().getUsername()).isEqualTo("admin");
    assertThat(result.get(1).getName()).isEqualTo("community2");
    assertThat(result.get(1).getDescription()).isEqualTo("des2");
    assertThat(result.get(1).getAdminUser()).isNull();
  }

  @Test
  public void create() {
    Long adminId = inTransaction(() -> userRepository.create(new User().setUsername("admin"))).getId();
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("community")
        .setDescription("description")
        .setTokenContractAddress("0x1234567")
        .setAdminUser(new User().setId(adminId))).getId());

    Community community = em().find(Community.class, id);

    assertThat(community.getName()).isEqualTo("community");
    assertThat(community.getDescription()).isEqualTo("description");
    assertThat(community.getTokenContractAddress()).isEqualTo("0x1234567");
    assertThat(community.getAdminUser().getUsername()).isEqualTo("admin");
  }


  @Test
  public void isAdmin() {
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));
    User user3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")));
    Community community1 = inTransaction(() -> repository.create(new Community().setName("community1").setAdminUser(user1)));
    Community community2 = inTransaction(() -> repository.create(new Community().setName("community2").setAdminUser(user2)));
    Community community3 = inTransaction(() -> repository.create(new Community().setName("community3")));

    boolean result = repository.isAdmin(user1.getId(), community1.getId());
    
    assertThat(result).isTrue();
    
    result = repository.isAdmin(user2.getId(), community1.getId());
    
    assertThat(result).isFalse();
    
    result = repository.isAdmin(user3.getId(), community3.getId());
    
    assertThat(result).isFalse();
  }
}