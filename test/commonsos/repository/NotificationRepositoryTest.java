package commonsos.repository;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import commonsos.repository.entity.Notification;

public class NotificationRepositoryTest extends RepositoryTest {

  NotificationRepository repository = new NotificationRepository(emService);
  CommunityRepository communityRepository = new CommunityRepository(emService);

  @Test
  public void findById() {
    // prepare
    Long id = inTransaction(() -> repository.create(
        new Notification()
          .setCommunityId(id("community"))
          .setTitle("title")
          .setUrl("url")
          .setCreatedBy(id("creator"))
          .setCreatedAt(Instant.parse("2018-12-01T00:00:00.00Z")))
        ).getId();

    // execute
    Optional<Notification> result = repository.findById(id);

    // verify
    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(id);
    assertThat(result.get().getCommunityId()).isEqualTo(id("community"));
    assertThat(result.get().getTitle()).isEqualTo("title");
    assertThat(result.get().getUrl()).isEqualTo("url");
    assertThat(result.get().getCreatedBy()).isEqualTo(id("creator"));
    assertThat(result.get().getCreatedAt()).isEqualTo(Instant.parse("2018-12-01T00:00:00.00Z"));
  }
  
  @Test
  public void findById_deleted() {
    // prepare
    Notification not_deleted = inTransaction(() -> repository.create(
        new Notification().setCommunityId(id("community")).setDeleted(false)));
    Notification deleted = inTransaction(() -> repository.create(
        new Notification().setCommunityId(id("community")).setDeleted(true)));

    // execute & verify
    Optional<Notification> result = repository.findById(not_deleted.getId());
    assertThat(result).isNotEmpty();
    
    result = repository.findById(deleted.getId());
    assertThat(result).isEmpty();
  }
  
  @Test
  public void findById_notFound() {
    // execute & verify
    Optional<Notification> result = repository.findById(id("not_exists"));
    assertThat(result).isEmpty();
  }
  
  @Test
  public void search() {
    // prepare
    inTransaction(() -> repository.create(new Notification().setCommunityId(id("c1")).setTitle("n1").setCreatedAt(Instant.now().plusSeconds(20))));
    inTransaction(() -> repository.create(new Notification().setCommunityId(id("c1")).setTitle("n2").setCreatedAt(Instant.now().plusSeconds(40))));
    inTransaction(() -> repository.create(new Notification().setCommunityId(id("c1")).setTitle("n3").setCreatedAt(Instant.now().plusSeconds(60))));
    inTransaction(() -> repository.create(new Notification().setCommunityId(id("c2")).setTitle("n4").setCreatedAt(Instant.now())));
    inTransaction(() -> repository.create(new Notification().setCommunityId(id("c1")).setTitle("n5").setCreatedAt(Instant.now()).setDeleted(true)));
    
    // execute
    List<Notification> result = repository.search(id("c1"));
    
    // verify
    assertThat(result.size()).isEqualTo(3);
    assertThat(result.get(0).getTitle()).isEqualTo("n3");
    assertThat(result.get(1).getTitle()).isEqualTo("n2");
    assertThat(result.get(2).getTitle()).isEqualTo("n1");
  }
}