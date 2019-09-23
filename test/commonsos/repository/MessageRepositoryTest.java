package commonsos.repository;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.util.MessageUtil;

public class MessageRepositoryTest extends AbstractRepositoryTest {

  private MessageRepository repository = spy(new MessageRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
  }
  
  @Test
  public void createMessage() {
    Message message = new Message()
      .setCreatedUserId(id("created by"))
      .setText("message text")
      .setThreadId(id("thread id"));

    Long id = inTransaction(() -> repository.create(message).getId());

    Message result = em().find(Message.class, id);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getCreatedUserId()).isEqualTo(id("created by"));
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
    // prepare
    // oldestMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedUserId(id("user1"))));
    // olderMessage
    inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedUserId(id("user2"))));
    // newestMessage
    Message newestMessage = inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedUserId(id("user3"))));

    // verify
    Optional<Message> result = repository.lastMessage(id("thread"));
    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(newestMessage.getId());

    // prepare
    // systemMessage
    Message systemMessage = inTransaction(() -> repository.create(new Message().setThreadId(id("thread")).setCreatedUserId(MessageUtil.getSystemMessageCreatorId())));
    // verify
    result = repository.lastMessage(id("thread"));
    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(systemMessage.getId());
  }

  @Test
  public void lastThreadMessage_noMessagesYet() {
    assertThat(repository.lastMessage(id("thread id"))).isEmpty();
  }
}