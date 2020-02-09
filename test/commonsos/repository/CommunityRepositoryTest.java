package commonsos.repository;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.PublishStatus;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.User;

public class CommunityRepositoryTest extends AbstractRepositoryTest {

  CommunityRepository repository = spy(new CommunityRepository(emService));
  UserRepository userRepository = spy(new UserRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
    doNothing().when(userRepository).checkLocked(any());
  }
  
  @Test
  public void findById() {
    // prepare
    User admin = inTransaction(() -> userRepository.create(new User().setUsername("admin")));
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setDescription("description")
        .setTokenContractAddress("66")
        .setAdminUser(admin)
        .setPublishStatus(PUBLIC))).getId();

    // execute
    Optional<Community> community = repository.findById(id);

    // verify
    assertThat(community).isNotEmpty();
    assertThat(community.get().getId()).isEqualTo(id);
    assertThat(community.get().getName()).isEqualTo("Kaga");
    assertThat(community.get().getPublishStatus()).isEqualTo(PUBLIC);
    assertThat(community.get().getDescription()).isEqualTo("description");
    assertThat(community.get().getTokenContractAddress()).isEqualTo("66");
    assertThat(community.get().getAdminUser().getId()).isEqualTo(admin.getId());
    assertThat(community.get().getAdminUser().getUsername()).isEqualTo("admin");

    // prepare
    id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setTokenContractAddress("66")
        .setPublishStatus(PRIVATE))).getId();

    // execute
    community = repository.findById(id);

    // verify
    assertThat(community).isNotEmpty();
    assertThat(community.get().getId()).isEqualTo(id);
    assertThat(community.get().getPublishStatus()).isEqualTo(PRIVATE);
  }

  @Test
  public void findById_deleted() {
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setPublishStatus(PUBLIC)
        .setDeleted(true))).getId();

    Optional<Community> community = repository.findById(id);
    assertThat(community).isEmpty();
  }

  @Test
  public void findPublicById() {
    // prepare
    User admin = inTransaction(() -> userRepository.create(new User().setUsername("admin")));
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setDescription("description")
        .setTokenContractAddress("66")
        .setAdminUser(admin)
        .setPublishStatus(PUBLIC))).getId();

    // execute
    Optional<Community> community = repository.findPublicById(id);

    // verify
    assertThat(community).isNotEmpty();
    assertThat(community.get().getId()).isEqualTo(id);
    assertThat(community.get().getName()).isEqualTo("Kaga");
    assertThat(community.get().getPublishStatus()).isEqualTo(PUBLIC);
    assertThat(community.get().getDescription()).isEqualTo("description");
    assertThat(community.get().getTokenContractAddress()).isEqualTo("66");
    assertThat(community.get().getAdminUser().getId()).isEqualTo(admin.getId());
    assertThat(community.get().getAdminUser().getUsername()).isEqualTo("admin");
  }

  @Test
  public void findPublicById_noAdmin() {
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setDescription("description")
        .setTokenContractAddress("66")
        .setPublishStatus(PUBLIC))).getId();

    Optional<Community> community = repository.findPublicById(id);

    assertThat(community).isNotEmpty();
    assertThat(community.get().getId()).isEqualTo(id);
    assertThat(community.get().getName()).isEqualTo("Kaga");
    assertThat(community.get().getDescription()).isEqualTo("description");
    assertThat(community.get().getTokenContractAddress()).isEqualTo("66");
    assertThat(community.get().getAdminUser()).isNull();
  }

  @Test
  public void findPublicById_private() {
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setPublishStatus(PRIVATE)
        .setDeleted(false))).getId();

    Optional<Community> community = repository.findPublicById(id);
    assertThat(community).isEmpty();
  }

  @Test
  public void findPublicById_deleted() {
    Long id = inTransaction(() -> repository.create(new Community()
        .setName("Kaga")
        .setPublishStatus(PUBLIC)
        .setDeleted(true))).getId();

    Optional<Community> community = repository.findPublicById(id);
    assertThat(community).isEmpty();
  }

  @Test
  public void findPublicById_notFound() {
    assertThat(repository.findPublicById(123L)).isEmpty();
  }

  @Test
  public void list() {
    // prepare
    inTransaction(() -> {
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community1"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community2"));
      em().persist(new Community().setPublishStatus(PRIVATE).setName("community3"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community4").setDeleted(true));
    });

    // execute
    ResultList<Community> result = repository.searchAll(null);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);
    assertThat(result.getList().get(0).getName()).isEqualTo("community1");
    assertThat(result.getList().get(1).getName()).isEqualTo("community2");
    assertThat(result.getList().get(2).getName()).isEqualTo("community3");
  }

  @Test
  public void list_pagination() {
    // prepare
    inTransaction(() -> {
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community1"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community2"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community3"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community4").setDeleted(true));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community5"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community6"));
    });

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.DESC);
    ResultList<Community> result = repository.searchAll(pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);
    assertThat(result.getList().get(0).getName()).isEqualTo("community6");
    assertThat(result.getList().get(1).getName()).isEqualTo("community5");
    assertThat(result.getList().get(2).getName()).isEqualTo("community3");

    // execute
    pagination.setPage(1);
    result = repository.searchAll(pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getName()).isEqualTo("community2");
    assertThat(result.getList().get(1).getName()).isEqualTo("community1");
  }

  @Test
  public void listPublic() {
    // prepare
    Long adminId = inTransaction(() -> userRepository.create(new User().setUsername("admin"))).getId();
    inTransaction(() -> {
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community1").setDescription("des1").setTokenContractAddress("66").setAdminUser(new User().setId(adminId)));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community2").setDescription("des2").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community3").setDescription("des3").setTokenContractAddress(null));
      em().persist(new Community().setPublishStatus(PRIVATE).setName("community4").setDescription("des4").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("community5").setDescription("des5").setTokenContractAddress("66").setDeleted(true));
    });

    // execute
    ResultList<Community> result = repository.searchPublic(null);

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
  public void listPublic_pagination() {
    // prepare
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community1").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community2").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community3").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community4").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community5").setTokenContractAddress("0x0")));

    //execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Community> result = repository.searchPublic(pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.searchPublic(pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void listPublic_filter() {
    // prepare
    inTransaction(() -> {
      em().persist(new Community().setPublishStatus(PUBLIC).setName("comm_foo").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("comm_Foo_bar").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("comm_bar").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("comm_bar_foo").setTokenContractAddress(null));
      em().persist(new Community().setPublishStatus(PRIVATE).setName("foo").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("foo").setTokenContractAddress("66").setDeleted(true));
    });

    // execute
    ResultList<Community> result = repository.searchPublic("foo", null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getName()).isEqualTo("comm_foo");
    assertThat(result.getList().get(1).getName()).isEqualTo("comm_Foo_bar");
  }

  @Test
  public void listPublic_filter_unicode() {
    // prepare
    inTransaction(() -> {
      em().persist(new Community().setPublishStatus(PUBLIC).setName("コミュニティ　フー").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("コミュニティ　フー　バー").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("コミュニティ　バー").setTokenContractAddress("66"));
      em().persist(new Community().setPublishStatus(PUBLIC).setName("コミュニティ　🍺").setTokenContractAddress("66"));
    });

    // execute
    ResultList<Community> result = repository.searchPublic("フー", null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getName()).isEqualTo("コミュニティ　フー");
    assertThat(result.getList().get(1).getName()).isEqualTo("コミュニティ　フー　バー");
    
    // execute
    result = repository.searchPublic("🍺", null); // 4 byte code

    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getList().get(0).getName()).isEqualTo("コミュニティ　🍺");
  }

  @Test
  public void listPublic_filter_pagination() {
    // prepare
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community1").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community2").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community3").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community4").setTokenContractAddress("0x0")));
    inTransaction(() -> em().persist(new Community().setPublishStatus(PUBLIC).setName("community5").setTokenContractAddress("0x0")));

    //execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Community> result = repository.searchPublic("community", pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.searchPublic("community", pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void listPublic_communityUser() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community3").setTokenContractAddress(null)));
    Community community4 = inTransaction(() -> repository.create(new Community().setPublishStatus(PRIVATE).setName("community4").setTokenContractAddress("0x0")));
    Community community5 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community5").setTokenContractAddress("0x0").setDeleted(true)));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4),
        new CommunityUser().setCommunity(community5)))));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));

    // execute
    ResultList<CommunityUser> result = repository.searchPublic(user1.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getCommunity().getName()).isEqualTo("community1");
    assertThat(result.getList().get(1).getCommunity().getName()).isEqualTo("community2");

    // execute
    result = repository.searchPublic(user2.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void listPublic_communityUser_pagination() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community3").setTokenContractAddress("0x0")));
    Community community4 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community4").setTokenContractAddress("0x0")));
    Community community5 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community5").setTokenContractAddress("0x0")));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4),
        new CommunityUser().setCommunity(community5)))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<CommunityUser> result = repository.searchPublic(user1.getCommunityUserList(), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.searchPublic(user1.getCommunityUserList(), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void listPublic_communityUser_filter() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community3").setTokenContractAddress(null)));
    Community community4 = inTransaction(() -> repository.create(new Community().setPublishStatus(PRIVATE).setName("community4").setTokenContractAddress("0x0")));
    Community community5 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community5").setTokenContractAddress("0x0").setDeleted(true)));
    Community dummy = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("dummy").setTokenContractAddress("0x0")));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4),
        new CommunityUser().setCommunity(community5),
        new CommunityUser().setCommunity(dummy)))));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));

    // execute
    ResultList<CommunityUser> result = repository.searchPublic("com", user1.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getCommunity().getName()).isEqualTo("community1");
    assertThat(result.getList().get(1).getCommunity().getName()).isEqualTo("community2");

    // execute
    result = repository.searchPublic("com", user2.getCommunityUserList(), null);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void listPublic_communityUser_filter_pagination() {
    // prepare
    Community community1 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community1").setTokenContractAddress("0x0")));
    Community community2 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community2").setTokenContractAddress("0x0")));
    Community community3 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community3").setTokenContractAddress("0x0")));
    Community community4 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community4").setTokenContractAddress("0x0")));
    Community community5 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community5").setTokenContractAddress("0x0")));
    Community dummy = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("dummy").setTokenContractAddress("0x0")));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1").setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2),
        new CommunityUser().setCommunity(community3),
        new CommunityUser().setCommunity(community4),
        new CommunityUser().setCommunity(community5),
        new CommunityUser().setCommunity(dummy)))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<CommunityUser> result = repository.searchPublic("com", user1.getCommunityUserList(), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.searchPublic("com", user1.getCommunityUserList(), pagination);

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
        .setAdminUser(new User().setId(adminId))
        .setPublishStatus(PublishStatus.PUBLIC)
        .setFee(new BigDecimal("1.5"))
        .setWordpressAccountId("wpId")
        .setWordpressAccountEmailAddress("wpAddr")).getId());

    Community community = em().find(Community.class, id);

    assertThat(community.getName()).isEqualTo("community");
    assertThat(community.getDescription()).isEqualTo("description");
    assertThat(community.getTokenContractAddress()).isEqualTo("0x1234567");
    assertThat(community.getAdminUser().getUsername()).isEqualTo("admin");
    assertThat(community.getPublishStatus()).isEqualTo(PublishStatus.PUBLIC);
    assertThat(community.getFee().floatValue()).isEqualTo(1.5F);
    assertThat(community.getWordpressAccountId()).isEqualTo("wpId");
    assertThat(community.getWordpressAccountEmailAddress()).isEqualTo("wpAddr");
  }

  @Test
  public void isAdmin() {
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));
    User user3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")));
    Community community1 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community1").setAdminUser(user1)));
    Community community2 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community2").setAdminUser(user2)));
    Community community3 = inTransaction(() -> repository.create(new Community().setPublishStatus(PUBLIC).setName("community3")));

    boolean result = repository.isAdmin(user1.getId(), community1.getId());
    
    assertThat(result).isTrue();
    
    result = repository.isAdmin(user2.getId(), community1.getId());
    
    assertThat(result).isFalse();
    
    result = repository.isAdmin(user3.getId(), community3.getId());
    
    assertThat(result).isFalse();
  }

  @Test
  public void isWordpressAccountIdTaken() {
    inTransaction(() -> repository.create(new Community().setName("community1").setWordpressAccountId("AAAAaa")));
    inTransaction(() -> repository.create(new Community().setName("community2").setWordpressAccountId("BBBbbB")));
    inTransaction(() -> repository.create(new Community().setName("community3").setWordpressAccountId("ccCCCC").setDeleted(true)));

    boolean result = repository.isWordpressAccountIdTaken("aaaaaa");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountIdTaken("AAAAAA");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountIdTaken("bbbbbb");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountIdTaken("cccccc");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountIdTaken("dddddd");
    assertThat(result).isFalse();
  }

  @Test
  public void isWordpressAccountEmailAddressTaken() {
    inTransaction(() -> repository.create(new Community().setName("community1").setWordpressAccountEmailAddress("AAAAaa")));
    inTransaction(() -> repository.create(new Community().setName("community2").setWordpressAccountEmailAddress("BBBbbB")));
    inTransaction(() -> repository.create(new Community().setName("community3").setWordpressAccountEmailAddress("ccCCCC").setDeleted(true)));

    boolean result = repository.isWordpressAccountEmailAddressTaken("aaaaaa");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountEmailAddressTaken("AAAAAA");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountEmailAddressTaken("bbbbbb");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountEmailAddressTaken("cccccc");
    assertThat(result).isTrue();
    result = repository.isWordpressAccountEmailAddressTaken("dddddd");
    assertThat(result).isFalse();
  }
}