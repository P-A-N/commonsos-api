package commonsos.repository;

import static commonsos.TestId.id;
import static commonsos.repository.entity.AdType.GIVE;
import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.User;

public class AdRepositoryTest extends AbstractRepositoryTest {

  private AdRepository repository = spy(new AdRepository(emService));
  private UserRepository userRepository = spy(new UserRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
    doNothing().when(userRepository).checkLocked(any());
  }
  
  @Test
  public void create() {
    Long id = inTransaction(() -> repository.create(new Ad()
        .setPublishStatus(PUBLIC)
        )).getId();

    Ad result = em().find(Ad.class, id);
    
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getPublishStatus()).isEqualTo(PUBLIC);
  }

  @Test
  public void searchByCommunityId() {
    Long id1 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setPublishStatus(PUBLIC)).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setPublishStatus(PUBLIC)).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setPublishStatus(PRIVATE)).getId());
    Long id4 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setDeleted(true).setPublishStatus(PUBLIC)).getId());
    Long id5 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community2")).setPublishStatus(PUBLIC)).getId());

    ResultList<Ad> result = repository.searchByCommunityId(id("community1"), null);

    assertThat(result.getList()).extracting("id").containsExactly(id1, id2, id3);
  }

  @Test
  public void searchPublicByCommunityId() {
    Long id1 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setPublishStatus(PUBLIC)).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setPublishStatus(PUBLIC)).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setPublishStatus(PRIVATE)).getId());
    Long id4 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setDeleted(true).setPublishStatus(PUBLIC)).getId());
    Long id5 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community2")).setPublishStatus(PUBLIC)).getId());

    ResultList<Ad> result = repository.searchPublicByCommunityId(id("community1"), null);

    assertThat(result.getList()).extracting("id").containsExactly(id1, id2);
  }

  @Test
  public void searchPublicByCommunityId_notFound() {
    ResultList<Ad> result = repository.searchPublicByCommunityId(id("community1"), null);

    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void searchPublicByCommunityId_filter_description() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId1).setDescription("text").setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId2).setDescription("text").setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setDeleted(true).setCreatedUserId(userId3).setDescription("text").setCommunityId(id("community1"))).getId());
    Long id4 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PRIVATE).setCreatedUserId(userId1).setDescription("text").setCommunityId(id("community1"))).getId());

    ResultList<Ad> result = repository.searchPublicByCommunityId(id("community1"), "text", null);

    assertThat(result.getList()).extracting("id").containsExactly(id1);
  }

  @Test
  public void searchPublicByCommunityId_filter_title() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId1).setTitle("_title_").setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId2).setTitle("_title_").setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId3).setTitle("_title_").setCommunityId(id("community3"))).getId());

    ResultList<Ad> result = repository.searchPublicByCommunityId(id("community1"), "title", null);

    assertThat(result.getList()).extracting("id").containsExactly(id1);
  }

  @Test
  public void searchPublicByCommunityId_filter_username() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId1).setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId2).setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCreatedUserId(userId3).setCommunityId(id("community3"))).getId());

    ResultList<Ad> result = repository.searchPublicByCommunityId(id("community1"), "user", null);

    assertThat(result.getList()).extracting("id").containsExactly(id1);
  }

  @Test
  public void searchPublicByCommunityId_filter_notFound() {
    ResultList<Ad> result = repository.searchPublicByCommunityId(id("community1"), "user", null);

    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void searchPublicByCommunityId_pagination() {
    // prepare
    inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setTitle("ad1").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setTitle("ad2").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setTitle("ad3").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setTitle("ad4").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setTitle("ad5").setCommunityId(id("community1"))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Ad> result = repository.searchPublicByCommunityId(id("community1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.searchPublicByCommunityId(id("community1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void searchByCreatorId() {
    Long id1 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PRIVATE).setCommunityId(id("community1")).setCreatedUserId(id("user1"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user1")).setDeleted(true)).getId());
    Long id4 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user2"))).getId());
    Long id5 = inTransaction(() -> repository.create(new Ad().setPublishStatus(PUBLIC).setCommunityId(id("community2")).setCreatedUserId(id("user1"))).getId());

    // execute
    ResultList<Ad> result = repository.searchByCreatorId(id("user1"), null);

    // verify
    assertThat(result.getList()).extracting("id").containsExactly(id1, id2, id5);
  }

  @Test
  public void searchByCreatorId_pagination() {
    // prepare
    inTransaction(() -> repository.create(new Ad().setTitle("ad1").setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad2").setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad3").setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad4").setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad5").setPublishStatus(PUBLIC).setCommunityId(id("community1")).setCreatedUserId(id("user1"))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Ad> result = repository.searchByCreatorId(id("user1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.searchByCreatorId(id("user1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void findById() {
    Long id = inTransaction(() -> repository.create(new Ad()
        .setTitle("Title")
        .setCreatedUserId(id("john"))
        .setPoints(TEN).setType(GIVE)
        .setPhotoUrl("url://photo")
        .setDescription("description")
        .setLocation("home")
        .setPublishStatus(PRIVATE))
      .getId());

    Ad result = repository.findById(id).get();

    assertThat(result.getTitle()).isEqualTo("Title");
    assertThat(result.getCreatedUserId()).isEqualTo(id("john"));
    assertThat(result.getType()).isEqualTo(GIVE);
    assertThat(result.getPhotoUrl()).isEqualTo("url://photo");
    assertThat(result.getCreatedAt()).isNotNull();
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getLocation()).isEqualTo("home");
    assertThat(result.isDeleted()).isEqualTo(false);
    assertThat(result.getPublishStatus()).isEqualTo(PRIVATE);
  }

  @Test
  public void findById_deleted() {
    Long id = inTransaction(() -> repository.create(new Ad().setDeleted(true)).getId());

    Optional<Ad> result = repository.findById(id);

    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void findById_notFound() {
    Optional<Ad> result = inTransaction(() -> repository.findById(id("unknown")));

    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void findPublicById() {
    Long id = inTransaction(() -> repository.create(new Ad().setPublishStatus(PRIVATE)).getId());

    // execute & verify
    Optional<Ad> result = repository.findPublicById(id);
    assertThat(result).isNotPresent();

    inTransaction(() -> repository.update(repository.findById(id).get().setPublishStatus(PUBLIC)));

    // execute & verify
    result = repository.findPublicById(id);
    assertThat(result).isPresent();
  }

  @Test
  public void update() {
    Ad testAd = inTransaction(() -> repository.create(new Ad()
        .setCreatedUserId(id("john"))
        .setType(GIVE)
        .setTitle("Title")
        .setDescription("description")
        .setPoints(TEN)
        .setLocation("home")))
        .setPhotoUrl("url://photo")
        .setCommunityId(id("community"));
    Instant createdAt = testAd.getCreatedAt();

    testAd.setTitle("Title2").setDescription("description2").setLocation("home2");
    inTransaction(() -> repository.update(testAd));
    
    Ad result = repository.findById(testAd.getId()).get();
    Instant updatedAt = result.getUpdatedAt();

    assertThat(result.getTitle()).isEqualTo("Title2");
    assertThat(result.getDescription()).isEqualTo("description2");
    assertThat(result.getLocation()).isEqualTo("home2");
    assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    
    inTransaction(() -> repository.update(testAd.setLocation("home2")));
    result = repository.findById(testAd.getId()).get();
    assertTrue(result.getUpdatedAt().isAfter(updatedAt));
  }
}