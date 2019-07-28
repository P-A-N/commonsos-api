package commonsos.repository;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;

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
    Message message = new Message()
      .setCreatedBy(id("created by"))
      .setText("message text")
      .setThreadId(id("thread id"));

    Long id = inTransaction(() -> repository.create(message).getId());

    Message result = em().find(Message.class, id);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getCreatedBy()).isEqualTo(id("created by"));
    assertThat(result.getText()).isEqualTo("message text");
    assertThat(result.getThreadId()).isEqualTo(id("thread id"));
  }

  @Test
  public void listByThread_olderMessagesFirst() {
    Long id1 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    inTransaction(() -> repository.create(new Message().setThreadId(id("other thread"))));

    ResultList<Message> result = repository.listByThread(id("thread id"), null);

    assertThat(result.getList()).extracting("id").containsExactly(id1, id2, id3);
  }

  @Test
  public void listByThread_pagination_asc() {
    // prepare
    Long id1 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id4 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id5 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<Message> result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id1, id2, id3);

    // execute
    pagination.setPage(1);
    result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id4, id5);
  }

  @Test
  public void listByThread_pagination_desc() {
    // prepare
    Long id1 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id4 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());
    Long id5 = inTransaction(() -> repository.create(new Message().setThreadId(id("thread id"))).getId());

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.DESC);
    ResultList<Message> result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id5, id4, id3);

    // execute
    pagination.setPage(1);
    result = repository.listByThread(id("thread id"), pagination);

    assertThat(result.getList()).extracting("id").containsExactly(id2, id1);
  }

  @Test
  public void lastThreadMessage() {
    // oldestMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(id("user1"))));
    // olderMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(id("user2"))));
    // newestMessage
    Message newestMessage = inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(id("user3"))));
    // systemMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedBy(MessageUtil.getSystemMessageCreatorId())));

    Optional<Message> result = repository.lastMessage(id("thread"));

    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(newestMessage.getId());
  }

  @Test
  public void lastThreadMessage_noMessagesYet() {
    assertThat(repository.lastMessage(id("thread id"))).isEmpty();
  }
}