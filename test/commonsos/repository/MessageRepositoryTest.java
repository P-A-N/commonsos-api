package commonsos.repository;

import static commonsos.TestId.id;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Message;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.service.command.PaginationCommand;
import commonsos.util.MessageUtil;

public class MessageRepositoryTest extends AbstractRepositoryTest {

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

    ResultList<Message> result = repository.listByThread(id("thread id"), null);

    assertThat(result.getList()).extracting("id").containsExactly(id3, id1, id2);
  }

  @Test
  public void listByThread_pagination_asc() {
    // prepare
    Long id1 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(1, HOURS))).getId());
    Long id2 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(2, HOURS))).getId());
    Long id3 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(3, HOURS))).getId());
    Long id4 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(4, HOURS))).getId());
    Long id5 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(5, HOURS))).getId());

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Message> result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id5, id4, id3);

    // execute
    pagination.setPage(1);
    result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id2, id1);
  }

  @Test
  public void listByThread_pagination_desc() {
    // prepare
    Long id1 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(1, HOURS))).getId());
    Long id2 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(2, HOURS))).getId());
    Long id3 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(3, HOURS))).getId());
    Long id4 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(4, HOURS))).getId());
    Long id5 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id")).setCreatedAt(now().minus(5, HOURS))).getId());

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.DESC);
    ResultList<Message> result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id1, id2, id3);

    // execute
    pagination.setPage(1);
    result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id4, id5);
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