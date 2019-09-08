package commonsos.repository;

import static commonsos.TestId.id;
import static commonsos.repository.entity.AdType.GIVE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.User;

public class AdRepositoryTest extends AbstractRepositoryTest {

  private AdRepository repository = new AdRepository(emService);
  private UserRepository userRepository = new UserRepository(emService);

  @Test
  public void create() {
    Long id = inTransaction(() -> repository.create(new Ad()).getId());

    assertThat(em().find(Ad.class, id)).isNotNull();
  }

  @Test
  public void ads() {
    Long id1 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setDeleted(true)).getId());
    Long id4 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community2"))).getId());

    ResultList<Ad> result = repository.ads(id("community1"), null);

    assertThat(result.getList()).extracting("id").containsExactly(id1, id2);
  }

  @Test
  public void ads_notFound() {
    ResultList<Ad> result = repository.ads(id("community1"), null);

    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void ads_filter_description() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setDescription("text").setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setDescription("text").setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setDescription("text").setCommunityId(id("community3"))).getId());

    ResultList<Ad> result = repository.ads(id("community1"), "text", null);

    assertThat(result.getList()).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_title() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setTitle("_title_").setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setTitle("_title_").setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setTitle("_title_").setCommunityId(id("community3"))).getId());

    ResultList<Ad> result = repository.ads(id("community1"), "title", null);

    assertThat(result.getList()).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_username() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setCommunityId(id("community3"))).getId());

    ResultList<Ad> result = repository.ads(id("community1"), "user", null);

    assertThat(result.getList()).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_deleted() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2").setDeleted(true)).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setCommunityId(id("community1"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setCommunityId(id("community1")).setDeleted(true)).getId());

    ResultList<Ad> result = repository.ads(id("community1"), "user", null);

    assertThat(result.getList()).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_notFound() {
    ResultList<Ad> result = repository.ads(id("community1"), "user", null);

    assertThat(result.getList().size()).isEqualTo(0);
  }

  @Test
  public void ads_pagination() {
    // prepare
    inTransaction(() -> repository.create(new Ad().setTitle("ad1").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad2").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad3").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad4").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad5").setCommunityId(id("community1"))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Ad> result = repository.ads(id("community1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.ads(id("community1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void myAds() {
    Long id1 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setCreatedBy(id("user1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setCreatedBy(id("user1"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setCreatedBy(id("user1")).setDeleted(true)).getId());
    Long id4 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setCreatedBy(id("user2"))).getId());
    Long id5 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community2")).setCreatedBy(id("user1"))).getId());

    // execute
    ResultList<Ad> result = repository.myAds(id("user1"), null);

    // verify
    assertThat(result.getList()).extracting("id").containsExactly(id1, id2, id5);
  }

  @Test
  public void myAds_pagination() {
    // prepare
    inTransaction(() -> repository.create(new Ad().setTitle("ad1").setCommunityId(id("community1")).setCreatedBy(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad2").setCommunityId(id("community1")).setCreatedBy(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad3").setCommunityId(id("community1")).setCreatedBy(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad4").setCommunityId(id("community1")).setCreatedBy(id("user1"))));
    inTransaction(() -> repository.create(new Ad().setTitle("ad5").setCommunityId(id("community1")).setCreatedBy(id("user1"))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Ad> result = repository.myAds(id("user1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.myAds(id("user1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void findById() {
    Long id = inTransaction(() -> repository.create(new Ad()
        .setTitle("Title")
        .setCreatedBy(id("john"))
        .setPoints(TEN).setType(GIVE)
        .setPhotoUrl("url://photo")
        .setDescription("description")
        .setLocation("home"))
      .getId());

    Ad result = repository.find(id).get();

    assertThat(result.getTitle()).isEqualTo("Title");
    assertThat(result.getCreatedBy()).isEqualTo(id("john"));
    assertThat(result.getType()).isEqualTo(GIVE);
    assertThat(result.getPhotoUrl()).isEqualTo("url://photo");
    assertThat(result.getCreatedAt()).isNotNull();
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getLocation()).isEqualTo("home");
    assertThat(result.isDeleted()).isEqualTo(false);
  }

  @Test
  public void findById_deleted() {
    Long id = inTransaction(() -> repository.create(new Ad().setDeleted(true)).getId());

    Optional<Ad> result = repository.find(id);

    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void findById_notFound() {
    Optional<Ad> result = inTransaction(() -> repository.find(id("unknown")));

    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void update() {
    Ad testAd = inTransaction(() -> repository.create(new Ad()
        .setCreatedBy(id("john"))
        .setType(GIVE)
        .setTitle("Title")
        .setDescription("description")
        .setPoints(TEN)
        .setLocation("home")))
        .setPhotoUrl("url://photo")
        .setCommunityId(id("community"));
    Instant createdAt = testAd.getCreatedAt();
    Instant updatedAt = testAd.getUpdatedAt();

    testAd.setTitle("Title2").setDescription("description2").setLocation("home2");
    inTransaction(() -> repository.update(testAd));
    
    Ad result = repository.find(testAd.getId()).get();

    assertThat(result.getTitle()).isEqualTo("Title2");
    assertThat(result.getDescription()).isEqualTo("description2");
    assertThat(result.getLocation()).isEqualTo("home2");
    assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    assertTrue(result.getUpdatedAt().isAfter(updatedAt));
  }
}