package commonsos.repository.message;

import static commonsos.TestId.id;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import commonsos.DBTest;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;

public class MessageThreadRepositoryTest extends DBTest {

  UserRepository userRepository = new UserRepository(entityManagerService);
  MessageThreadRepository repository = new MessageThreadRepository(entityManagerService);
  MessageRepository messageRepository = new MessageRepository(entityManagerService);

  @Test
  public void byAdId() {
    inTransaction(() -> repository.create(new MessageThread().setAdId(10L).setCreatedBy(id("me"))));
    inTransaction(() -> repository.create(new MessageThread().setAdId(20L).setCreatedBy(id("other-user"))));
    Long id = inTransaction(() -> repository.create(new MessageThread().setAdId(20L).setCreatedBy(id("me")))).getId();

    Optional<MessageThread> result = repository.byAdId(new User().setId(id("me")), 20L);
    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(id);
  }

  @Test
  public void byAdId_notFound() {
    assertThat(repository.byAdId(new User().setId(id("me")), 20L)).isEmpty();
  }

  @Test
  public void create() {
    User myself = inTransaction(() -> userRepository.create(new User().setUsername("myself")));
    User counterparty = inTransaction(() -> userRepository.create(new User().setUsername("counterparty")));

    List<MessageThreadParty> parties = asList(party(myself), party(counterparty));
    MessageThread messageThread = new MessageThread().setParties(parties);
    Long id = inTransaction(() -> repository.create(messageThread).getId());

    MessageThread result = em().find(MessageThread.class, id);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();

    MessageThreadParty party1 = result.getParties().get(0);
    MessageThreadParty party2 = result.getParties().get(1);

    assertThat(party1.getUser().getUsername()).isEqualTo("myself");
    assertThat(party1.getVisitedAt()).isNull();

    assertThat(party2.getUser().getUsername()).isEqualTo("counterparty");
    assertThat(party2.getVisitedAt()).isNull();
  }

  private MessageThreadParty party(User myself) {
    return new MessageThreadParty().setUser(myself);
  }

  @Test
  public void listByUser() {
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    User otherUser = inTransaction(() -> userRepository.create(new User().setUsername("otherUser")));

    MessageThread thread1 = new MessageThread().setParties(asList(party(user), party(otherUser)));
    MessageThread thread2 = new MessageThread().setParties(asList(party(otherUser)));
    MessageThread thread3 = new MessageThread().setParties(asList(party(otherUser), party(user)));

    Long id1 = inTransaction(() -> repository.create(thread1).getId());
    Long id2 = inTransaction(() -> repository.create(thread2).getId());
    Long id3 = inTransaction(() -> repository.create(thread3).getId());

    List<MessageThread> result = repository.listByUser(user);

    assertThat(result).extracting("id").containsExactly(id1, id3);
  }

  @Test
  public void byUserId() {
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    User otherUser1 = inTransaction(() -> userRepository.create(new User().setUsername("otherUser1")));
    User otherUser2 = inTransaction(() -> userRepository.create(new User().setUsername("otherUser2")));

    MessageThread thread0 = new MessageThread().setParties(asList(party(user), party(otherUser1))).setGroup(true);
    MessageThread thread1 = new MessageThread().setParties(asList(party(user), party(otherUser1))).setGroup(false);
    MessageThread thread2 = new MessageThread().setParties(asList(party(user), party(otherUser2)));
    MessageThread thread3 = new MessageThread().setParties(asList(party(otherUser1), party(otherUser2)));
    MessageThread thread4 = new MessageThread().setParties(asList(party(user), party(otherUser1))).setAdId(id("ad id"));

    inTransaction(() -> repository.create(thread0).getId());
    Long thread1Id = inTransaction(() -> repository.create(thread1).getId());
    inTransaction(() -> repository.create(thread2).getId());
    inTransaction(() -> repository.create(thread3).getId());
    inTransaction(() -> repository.create(thread4).getId());


    Optional<MessageThread> result = repository.betweenUsers(user.getId(), otherUser1.getId());


    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(thread1Id);
  }

  @Test
  public void threadById() {
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    Long id = inTransaction(() -> repository.create(new MessageThread().setParties(asList(party(user)))).getId());

    Optional<MessageThread> result = repository.thread(id);

    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(id);
  }

  @Test
  public void threadById_notFound() {
    assertThat(repository.thread(1L)).isEmpty();
  }

  @Test
  public void updateParty() {
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    MessageThreadParty party = new MessageThreadParty().setUser(user);
    MessageThread thread = new MessageThread().setParties(asList(party));
    inTransaction(() -> repository.create(thread).getId());

    party.setVisitedAt(now());
    inTransaction(() -> repository.update(party));

    MessageThreadParty actual = em().find(MessageThreadParty.class, party.getId());
    assertThat(actual.getVisitedAt()).isCloseTo(now(), within(1, SECONDS));
  }

  @Test
  public void updateThread() {
    User user = inTransaction(() -> userRepository.create(new User().setUsername("first")));
    MessageThread originalThread = new MessageThread().setParties(asList(new MessageThreadParty().setUser(user)));
    Long threadId = inTransaction(() -> repository.create(originalThread).getId());

    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("second")));
    MessageThread storedThread = repository.thread(threadId).orElseThrow(RuntimeException::new);

    List<MessageThreadParty> parties = new ArrayList<>(originalThread.getParties());
    parties.add(new MessageThreadParty().setUser(user2));
    storedThread.setParties(parties);


    inTransaction(() -> repository.update(storedThread));


    MessageThread result = repository.thread(threadId).orElseThrow(RuntimeException::new);
    assertThat(result.getParties()).hasSize(2);
    assertThat(result.getParties().get(0).getUser().getUsername()).isEqualTo("first");
    assertThat(result.getParties().get(1).getUser().getUsername()).isEqualTo("second");
  }

  @Test
  public void deleteMessageThreadParty() {
    // prepare
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));
    User user3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")));
    User user4 = inTransaction(() -> userRepository.create(new User().setUsername("user4")));
    
    MessageThread adMessageThread1 = inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setGroup(false)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread1.getId()).setUser(user1)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread1.getId()).setUser(user2)));

    MessageThread adMessageThread2 = inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad2")).setGroup(false)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread2.getId()).setUser(user1)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread2.getId()).setUser(user2)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread2.getId()).setUser(user3)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread2.getId()).setUser(user4)));

    MessageThread groupMessageThread1 = inTransaction(() -> repository.create(new MessageThread().setAdId(null).setGroup(true)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(groupMessageThread1.getId()).setUser(user1)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(groupMessageThread1.getId()).setUser(user2)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(groupMessageThread1.getId()).setUser(user3)));

    MessageThread betweenUserMessageThread1 = inTransaction(() -> repository.create(new MessageThread().setAdId(null).setGroup(false)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(betweenUserMessageThread1.getId()).setUser(user1)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(betweenUserMessageThread1.getId()).setUser(user2)));
    
    // verify before execute
    assertThat(em().find(MessageThread.class, adMessageThread1.getId()).getParties().size()).isEqualTo(2);
    assertThat(em().find(MessageThread.class, adMessageThread2.getId()).getParties().size()).isEqualTo(4);
    assertThat(em().find(MessageThread.class, groupMessageThread1.getId()).getParties().size()).isEqualTo(3);
    assertThat(em().find(MessageThread.class, betweenUserMessageThread1.getId()).getParties().size()).isEqualTo(2);
    
    // execute
    int result = inTransaction(() -> repository.deleteMessageThreadParty(user4));
    
    // verify
    assertThat(result).isEqualTo(1);
    assertThat(em().find(MessageThread.class, adMessageThread1.getId()).getParties().size()).isEqualTo(2);
    assertThat(em().find(MessageThread.class, adMessageThread2.getId()).getParties().size()).isEqualTo(3);
    assertThat(em().find(MessageThread.class, groupMessageThread1.getId()).getParties().size()).isEqualTo(3);
    assertThat(em().find(MessageThread.class, betweenUserMessageThread1.getId()).getParties().size()).isEqualTo(2);

    // execute
    result = inTransaction(() -> repository.deleteMessageThreadParty(user1));
    
    // verify
    assertThat(result).isEqualTo(3);
    assertThat(em().find(MessageThread.class, adMessageThread1.getId()).getParties().size()).isEqualTo(1);
    assertThat(em().find(MessageThread.class, adMessageThread2.getId()).getParties().size()).isEqualTo(2);
    assertThat(em().find(MessageThread.class, groupMessageThread1.getId()).getParties().size()).isEqualTo(2);
    assertThat(em().find(MessageThread.class, betweenUserMessageThread1.getId()).getParties().size()).isEqualTo(2);

    // execute
    result = inTransaction(() -> repository.deleteMessageThreadParty(user1));
    
    // verify
    assertThat(result).isEqualTo(0);
  }
  
  @Test
  public void unreadMessageThreadCount() {
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));

    Long threadId1 = threadWithMessages(user, user2, null).getId();
    Long threadId2 = threadWithMessages(user, user2, now().minus(10, SECONDS)).getId();
    threadWithMessages(user, user2, now().plus(10, SECONDS));

    List<Long> result = repository.unreadMessageThreadIds(user);

    assertThat(result).containsExactly(threadId1, threadId2);
  }

  @Test
  public void unreadMessageThreadCount_excludesThreadsWithoutMessages() {
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));

    MessageThreadParty myParty = new MessageThreadParty().setUser(user);
    MessageThreadParty counterParty = new MessageThreadParty().setUser(user2);
    MessageThread thread = new MessageThread().setParties(asList(myParty, counterParty));
    inTransaction(() -> repository.create(thread));

    List<Long> result = repository.unreadMessageThreadIds(user);

    assertThat(result).isEmpty();
  }

  private MessageThread threadWithMessages(User myUser, User otherUser, Instant visitedAt) {
    MessageThreadParty myParty = new MessageThreadParty().setUser(myUser).setVisitedAt(visitedAt);
    MessageThreadParty counterParty = new MessageThreadParty().setUser(otherUser);
    MessageThread thread = new MessageThread().setParties(asList(myParty, counterParty));
    inTransaction(() -> {
      repository.create(thread);
      messageRepository.create(new Message().setThreadId(thread.getId()).setCreatedAt(now())).setCreatedBy(otherUser.getId());
      messageRepository.create(new Message().setThreadId(thread.getId()).setCreatedAt(now())).setCreatedBy(myUser.getId());
    });

    return thread;
  }
}