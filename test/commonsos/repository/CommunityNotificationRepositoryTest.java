package commonsos.repository;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.CommunityNotification;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;

public class CommunityNotificationRepositoryTest extends AbstractRepositoryTest {

  private CommunityNotificationRepository repository = spy(new CommunityNotificationRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
  }
  
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
    ResultList<CommunityNotification> result = repository.findByCommunityId(id("community1"), null);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getWordpressId()).isEqualTo("wordPress1");
    assertThat(result.getList().get(1).getWordpressId()).isEqualTo("wordPress2");
  }

  @Test
  public void findByCommunityId_pagination() {
    // prepare
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress1").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress2").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress3").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress4").setCommunityId(id("community1"))));
    inTransaction(() -> repository.create(new CommunityNotification().setWordpressId("wordPress5").setCommunityId(id("community1"))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<CommunityNotification> result = repository.findByCommunityId(id("community1"), pagination);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.findByCommunityId(id("community1"), pagination);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }
}