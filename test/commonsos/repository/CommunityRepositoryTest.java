package commonsos.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.User;
import commonsos.service.command.PaginationCommand;

public class CommunityRepositoryTest extends AbstractRepositoryTest {

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
    // prepare
    Long adminId = inTransaction(() -> userRepository.create(new User().setUsername("admin"))).getId();
    inTransaction(() -> {
      em().persist(new Community().setName("community1").setDescription("des1").setTokenContractAddress("66").setAdminUser(new User().setId(adminId)));
      em().persist(new Community().setName("community2").setDescription("des2").setTokenContractAddress("66"));
      em().persist(new Community().setName("community3").setDescription("des3").setTokenContractAddress(null));
    });

    // execute
    ResultList<Community> result = repository.list(null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getName()).isEqualTo("community1");
    assertThat(result.getList().get(0).getDescription()).isEqualTo("des1");
    assertThat(result.getList().get(0).getAdminUser().getUsername()).isEqualTo("admin");
    assertThat(result.getList().get(1).getName()).isEqualTo("community2");
    assertThat(result.getList().get(1).getDescription()).isEqualTo("des2");
    assertThat(result.getList().get(1).getAdminUser()).isNull();
  }

  @Test
  public void list_pagination() {
    // prepare
    inTransaction(() -> em().persist(new Community().setName("community1").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community2").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community3").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community4").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community5").setTokenContractAddress("0x0")));

    //execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Community> result = repository.list(pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.list(pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void list_filter() {
    // prepare
    inTransaction(() -> {
      em().persist(new Community().setName("comm_foo").setTokenContractAddress("66"));
      em().persist(new Community().setName("comm_Foo_bar").setTokenContractAddress("66"));
      em().persist(new Community().setName("comm_bar").setTokenContractAddress("66"));
      em().persist(new Community().setName("comm_bar_foo").setTokenContractAddress(null));
    });

    // execute
    ResultList<Community> result = repository.list("foo", null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getName()).isEqualTo("comm_foo");
    assertThat(result.getList().get(1).getName()).isEqualTo("comm_Foo_bar");
  }

  @Test
  public void list_filter_unicode() {
    // prepare
    inTransaction(() -> {
      em().persist(new Community().setName("コミュニティ　フー").setTokenContractAddress("66"));
      em().persist(new Community().setName("コミュニティ　フー　バー").setTokenContractAddress("66"));
      em().persist(new Community().setName("コミュニティ　バー").setTokenContractAddress("66"));
      em().persist(new Community().setName("コミュニティ　🍺").setTokenContractAddress("66"));
    });

    // execute
    ResultList<Community> result = repository.list("フー", null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getName()).isEqualTo("コミュニティ　フー");
    assertThat(result.getList().get(1).getName()).isEqualTo("コミュニティ　フー　バー");
    
    // execute
    result = repository.list("🍺", null); // 4 byte code

    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getList().get(0).getName()).isEqualTo("コミュニティ　🍺");
  }

  @Test
  public void list_filter_pagination() {
    // prepare
    inTransaction(() -> em().persist(new Community().setName("community1").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community2").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community3").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community4").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setName("community5").setTokenContractAddress("0x0")));

    //execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Community> result = repository.list("community", pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.list("community", pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void list_communityUser() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setName("community3").setTokenContractAddress(null)));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3)))));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));

    // execute
    ResultList<CommunityUser> result = repository.list(user1.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getCommunity().getName()).isEqualTo("community1");
    assertThat(result.getList().get(1).getCommunity().getName()).isEqualTo("community2");

    // execute
    result = repository.list(user2.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void list_communityUser_pagination() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setName("community3").setTokenContractAddress("0x0")));
    Community community4 = inTransaction(() -> repository.create(new Community().setName("community4").setTokenContractAddress("0x0")));
    Community community5 = inTransaction(() -> repository.create(new Community().setName("community5").setTokenContractAddress("0x0")));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4),
        new CommunityUser().setCommunity(community5)))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<CommunityUser> result = repository.list(user1.getCommunityUserList(), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.list(user1.getCommunityUserList(), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void list_communityUser_filter() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setName("community3").setTokenContractAddress(null)));
    Community dummy = inTransaction(() -> repository.create(new Community().setName("dummy").setTokenContractAddress("0x0")));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(dummy)))));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));

    // execute
    ResultList<CommunityUser> result = repository.list("com", user1.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getCommunity().getName()).isEqualTo("community1");
    assertThat(result.getList().get(1).getCommunity().getName()).isEqualTo("community2");

    // execute
    result = repository.list("com", user2.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void list_communityUser_filter_pagination() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setName("community3").setTokenContractAddress("0x0")));
    Community community4 = inTransaction(() -> repository.create(new Community().setName("community4").setTokenContractAddress("0x0")));
    Community community5 = inTransaction(() -> repository.create(new Community().setName("community5").setTokenContractAddress("0x0")));
    Community dummy = inTransaction(() -> repository.create(new Community().setName("dummy").setTokenContractAddress("0x0")));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4),
        new CommunityUser().setCommunity(community5),
        new CommunityUser().setCommunity(dummy)))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<CommunityUser> result = repository.list("com", user1.getCommunityUserList(), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.list("com", user1.getCommunityUserList(), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
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