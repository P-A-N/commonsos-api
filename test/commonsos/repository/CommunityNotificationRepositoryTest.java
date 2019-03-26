package commonsos.repository;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.CommunityNotification;

public class CommunityNotificationRepositoryTest extends RepositoryTest {

  private CommunityNotificationRepository repository = new CommunityNotificationRepository(emService);

  @Test
  public void findByWordPressId() {
    // prepare
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress1")));

    // execute
    Optional<CommunityNotification> result = repository.findByWordPressId("wordPress1");
    
    // verify
    assertThat(result.isPresent()).isTrue();
  }
  
  @Test
  public void findByWordPressId_notFound() {
    // prepare
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress1")));

    // execute
    Optional<CommunityNotification> result = repository.findByWordPressId("invalid_wordPress1");
    
    // verify
    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void findByWordPressId_duplicate() {
    // prepare
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress1")));
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress1")));

    // execute
    Optional<CommunityNotification> result = repository.findByWordPressId("wordPress1");
    
    // verify
    assertThat(result.isPresent()).isTrue();
  }

  @Test
  public void findByCommunityId() {
    // prepare
    inTransaction(() -> repository.create(new CommunityNotification()
        .setWordpressId("wordPress1")
        .setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new CommunityNotification()
        .setWordpressId("wordPress2")
        .setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new CommunityNotification()
        .setWordpressId("wordPress3")
        .setCommunityId(id("community2"))));

    // execute
    List<CommunityNotification> result = repository.findByCommunityId(id("community1"));
    
    // verify
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getWordpressId()).isEqualTo("wordPress1");
    assertThat(result.get(1).getWordpressId()).isEqualTo("wordPress2");
  }
}