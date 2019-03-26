package commonsos.repository;

import static commonsos.TestId.id;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Message;
import commonsos.util.MessageUtil;

public class MessageRepositoryTest extends RepositoryTest {

  private MessageRepository repository = new MessageRepository(emService);

  @Test
  public void createMessage() {
    Instant now = now();
    Message message = new Message()
      .setCreatedBy(id("created by"))
      .setCreatedAt(now)
      .setText("message text")
      .setThreadId(id("thread id"));

    Long id = inTransaction(() -> repository.create(message).getId());

    Message result = em().find(Message.class, id);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getCreatedBy()).isEqualTo(id("created by"));
    assertThat(result.getCreatedAt()).isEqualTo(now);
    assertThat(result.getText()).isEqualTo("message text");
    assertThat(result.getThreadId()).isEqualTo(id("thread id"));
  }

  @Test
  public void listByThread_olderMessagesFirst() {
    Long id1 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(1, HOURS))).getId());
    Long id2 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now())).getId());
    Long id3 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(2, HOURS))).getId());
    inTransaction(() -> repository.create(new Message().setThreadId(id("other thread"))));

    List<Message> result = repository.listByThread(id("thread id"));

    assertThat(result).extracting("id").containsExactly(id3, id1, id2);
  }

  @Test
  public void lastThreadMessage() {
    // oldestMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(id("user1")).setCreatedAt(now().minus(3, HOURS))));
    // olderMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(id("user2")).setCreatedAt(now().minus(2, HOURS))));
    // newestMessage
    Message newestMessage = inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(id("user3")).setCreatedAt(now().minus(1, HOURS))));
    // systemMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(MessageUtil.getSystemMessageCreatorId())).setCreatedAt(now()));

    Optional<Message> result = repository.lastMessage(id("thread"));

    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(newestMessage.getId());
  }

  @Test
  public void lastThreadMessage_noMessagesYet() {
    assertThat(repository.lastMessage(id("thread id"))).isEmpty();
  }
}