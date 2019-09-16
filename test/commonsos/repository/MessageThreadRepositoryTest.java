package commonsos.repository;

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

import org.junit.jupiter.api.Test;

import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.User;

public class MessageThreadRepositoryTest extends AbstractRepositoryTest {

  MessageThreadRepository repository = new MessageThreadRepository(emService);
  UserRepository userRepository = new UserRepository(emService);
  CommunityRepository communityRepository = new CommunityRepository(emService);
  MessageRepository messageRepository = new MessageRepository(emService);

  @Test
  public void byCreaterAndAdId() {
    // prepare
    User user1 = new User().setId(id("user1"));
    User user2 = new User().setId(id("user2"));
    User user3 = new User().setId(id("user3"));
    MessageThread mt1 = inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user1")).setTitle("mt1")));
    MessageThread mt2 = inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user2")).setTitle("mt2")));

    // execute & verify
    Optional<MessageThread> result = repository.byCreaterAndAdId(user1.getId(), id("ad1"));
    assertThat(result.get().getTitle()).isEqualTo("mt1");

    // execute & verify
    result = repository.byCreaterAndAdId(user2.getId(), id("ad1"));
    assertThat(result.get().getTitle()).isEqualTo("mt2");

    // execute & verify
    result = repository.byCreaterAndAdId(user3.getId(), id("ad1"));
    assertThat(result).isEmpty();
    
    // prepare
    inTransaction(() -> repository.update(mt1.setDeleted(true)));

    // execute & verify
    result = repository.byCreaterAndAdId(user1.getId(), id("ad1"));
    assertThat(result).isEmpty();
    
    // execute & verify
    result = repository.byCreaterAndAdId(user2.getId(), id("ad2"));
    assertThat(result).isEmpty();
  }

  @Test
  public void byAdId() {
    // prepare
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user1")).setTitle("mt1_1")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user2")).setTitle("mt1_2")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user3")).setTitle("mt1_3").setDeleted(true)));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad2")).setCreatedBy(id("user1")).setTitle("mt2_1")));

    // execute & verify
    ResultList<MessageThread> result = repository.byAdId(id("ad1"), null);
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getTitle()).isEqualTo("mt1_1");
    assertThat(result.getList().get(1).getTitle()).isEqualTo("mt1_2");
  }

  @Test
  public void byAdId_pagination() {
    // prepare
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user1")).setTitle("mt1_1")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user2")).setTitle("mt1_2")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user3")).setTitle("mt1_3")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user4")).setTitle("mt1_4")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user5")).setTitle("mt1_5")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user6")).setTitle("mt1_6")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user7")).setTitle("mt1_7")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user8")).setTitle("mt1_8")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user9")).setTitle("mt1_9")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user10")).setTitle("mt1_10")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user11")).setTitle("mt1_11")));
    inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setCreatedBy(id("user12")).setTitle("mt1_12")));

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(10).setSort(SortType.ASC);
    ResultList<MessageThread> result = repository.byAdId(id("ad1"), pagination);
    assertThat(result.getList().size()).isEqualTo(10);
    assertThat(result.getList().get(0).getTitle()).isEqualTo("mt1_1");
    assertThat(result.getList().get(9).getTitle()).isEqualTo("mt1_10");

    // execute & verify
    pagination.setPage(1);
    result = repository.byAdId(id("ad1"), pagination);
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getTitle()).isEqualTo("mt1_11");
    assertThat(result.getList().get(1).getTitle()).isEqualTo("mt1_12");
    
    // execute & verify
    pagination.setPage(0).setSort(SortType.DESC);
    result = repository.byAdId(id("ad1"), pagination);
    assertThat(result.getList().size()).isEqualTo(10);
    assertThat(result.getList().get(0).getTitle()).isEqualTo("mt1_12");
    assertThat(result.getList().get(9).getTitle()).isEqualTo("mt1_3");

    // execute & verify
    pagination.setPage(1);
    result = repository.byAdId(id("ad1"), pagination);
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getTitle()).isEqualTo("mt1_2");
    assertThat(result.getList().get(1).getTitle()).isEqualTo("mt1_1");
  }

  @Test
  public void betweenUsers() {
    // prepare
    Community community1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")));
    Community community2 = inTransaction(() -> communityRepository.create(new Community().setName("community2")));
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    User otherUser1 = inTransaction(() -> userRepository.create(new User().setUsername("otherUser1")));
    User otherUser2 = inTransaction(() -> userRepository.create(new User().setUsername("otherUser2")));

    MessageThread thread0 = new MessageThread().setCommunityId(community1.getId()).setParties(asList(party(user), party(otherUser1))).setGroup(true);
    MessageThread thread1 = new MessageThread().setCommunityId(community1.getId()).setParties(asList(party(user), party(otherUser1))).setGroup(false);
    MessageThread thread2 = new MessageThread().setCommunityId(community1.getId()).setParties(asList(party(user), party(otherUser1))).setGroup(false).setDeleted(true);
    MessageThread thread3 = new MessageThread().setCommunityId(community1.getId()).setParties(asList(party(user), party(otherUser2)));
    MessageThread thread4 = new MessageThread().setCommunityId(community1.getId()).setParties(asList(party(otherUser1), party(otherUser2)));
    MessageThread thread5 = new MessageThread().setCommunityId(community1.getId()).setParties(asList(party(user), party(otherUser1))).setAdId(id("ad id"));

    inTransaction(() -> repository.create(thread0));
    Long thread1Id = inTransaction(() -> repository.create(thread1)).getId();
    inTransaction(() -> repository.create(thread2));
    inTransaction(() -> repository.create(thread3));
    inTransaction(() -> repository.create(thread4));
    inTransaction(() -> repository.create(thread5));

    // execute
    Optional<MessageThread> result = repository.betweenUsers(user.getId(), otherUser1.getId(), community1.getId());

    // verity
    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(thread1Id);

    // execute
    result = repository.betweenUsers(user.getId(), otherUser1.getId(), community2.getId());

    // verity
    assertThat(result).isEmpty();
  }

  @Test
  public void create() {
    // prepare
    User myself = inTransaction(() -> userRepository.create(new User().setUsername("myself")));
    User counterparty = inTransaction(() -> userRepository.create(new User().setUsername("counterparty")));

    // execute
    List<MessageThreadParty> parties = asList(party(myself), party(counterparty));
    MessageThread messageThread = new MessageThread().setParties(parties);
    Long id = inTransaction(() -> repository.create(messageThread).getId());

    // verify
    MessageThread result = em().find(MessageThread.class, id);
    result.getParties().sort((p1,p2) -> p1.getId().compareTo(p2.getId()));
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
  public void listByUserAndMemberAndMessage() {
    // prepare
    Long communityId1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")).getId());
    Long communityId2 = inTransaction(() -> communityRepository.create(new Community().setName("community2")).getId());
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    User otherUser1 = inTransaction(() -> userRepository.create(new User().setUsername("otherUser1")));
    User otherUser2 = inTransaction(() -> userRepository.create(new User().setUsername("otherUser2")));

    MessageThread thread1 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId1).setParties(asList(party(user), party(otherUser1), party(otherUser2)))));
    MessageThread thread2 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId1).setParties(asList(party(user), party(otherUser1)))));
    MessageThread thread3 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId1).setParties(asList(party(user), party(otherUser2)))));
    MessageThread thread4 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId1).setParties(asList(party(otherUser1), party(otherUser2)))));
    MessageThread thread5 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId2).setParties(asList(party(user), party(otherUser1), party(otherUser2)))));

    // [execute and verify] search by member
    ResultList<MessageThread> result = repository.listByUser(user, communityId1, "user1", null, null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId(), thread2.getId());

    result = repository.listByUser(user, communityId1, "user2", null, null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId(), thread3.getId());

    result = repository.listByUser(user, communityId1, "her", null, null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId(), thread2.getId(), thread3.getId());

    result = repository.listByUser(user, communityId1, "not_exists", null, null);
    assertThat(result.getList()).extracting("id").containsExactly();

    // prepare
    inTransaction(() -> messageRepository.create(new Message().setThreadId(thread1.getId()).setText("foobar")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(thread2.getId()).setText("foobar")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(thread3.getId()).setText("foobar")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(thread4.getId()).setText("foobar")));
    
    // [execute and verify] search by message
    result = repository.listByUser(user, communityId1, null, "oob", null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId(), thread2.getId(), thread3.getId());

    // prepare
    inTransaction(() -> messageRepository.create(new Message().setThreadId(thread1.getId()).setText("HOGE")));

    // [execute and verify] search by message
    result = repository.listByUser(user, communityId1, null, "OG", null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId());
    
    result = repository.listByUser(user, communityId1, null, "og", null);
    assertThat(result.getList()).extracting("id").containsExactly();

    // prepare multiple byte code
    inTransaction(() -> messageRepository.create(new Message().setThreadId(thread1.getId()).setText("こんにちわ。今日はいい天気ですね。")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(thread2.getId()).setText("こんにちわ。昨日はいい天気でしたね。")));
    
    // [execute and verify] search by message
    result = repository.listByUser(user, communityId1, null, "今日", null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId());

    result = repository.listByUser(user, communityId1, null, "こんにちわ", null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId(), thread2.getId());

    // [execute and verify] search by member and message
    result = repository.listByUser(user, communityId1, "user2", "こんにちわ", null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId());

    // [execute and verify] not specified member or message
    result = repository.listByUser(user, communityId1, "", "", null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId(), thread2.getId(), thread3.getId());
    result = repository.listByUser(user, communityId1, null, null, null);
    assertThat(result.getList()).extracting("id").containsExactly(thread1.getId(), thread2.getId(), thread3.getId());

    // [execute and verify] sql injection
    result = repository.listByUser(user, communityId1, "' OR TRUE", null, null);
    assertThat(result.getList()).extracting("id").containsExactly();
    result = repository.listByUser(user, communityId1, "; select * from message_threads", null, null);
    assertThat(result.getList()).extracting("id").containsExactly();

    // prepare deleted threads
    inTransaction(() -> repository.update(thread1.setDeleted(true)));
    
    // [execute and verify] not specified member or message
    result = repository.listByUser(user, communityId1, null, null, null);
    assertThat(result.getList()).extracting("id").containsExactly(thread2.getId(), thread3.getId());
  }

  @Test
  public void listByUserAndMemberAndMessage_pagination() {
    // prepare
    Long communityId = inTransaction(() -> communityRepository.create(new Community().setName("community")).getId());
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    Long id1 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId).setParties(asList(party(user)))).getId());
    Long id2 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId).setParties(asList(party(user)))).getId());
    Long id3 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId).setParties(asList(party(user)))).getId());
    Long id4 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId).setParties(asList(party(user)))).getId());
    Long id5 = inTransaction(() -> repository.create(new MessageThread().setCommunityId(communityId).setParties(asList(party(user)))).getId());
    inTransaction(() -> messageRepository.create(new Message().setThreadId(id1).setText("message")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(id2).setText("message")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(id3).setText("message")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(id4).setText("message")));
    inTransaction(() -> messageRepository.create(new Message().setThreadId(id5).setText("message")));

    //execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<MessageThread> result = repository.listByUser(user, communityId, "user", "message", pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.listByUser(user, communityId, "user", "message", pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void findById() {
    // prepare
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    MessageThread messageThread = inTransaction(() -> repository.create(new MessageThread().setTitle("mt").setParties(asList(party(user)))));

    // execute
    Optional<MessageThread> result = repository.findById(messageThread.getId());

    // verify
    assertThat(result).isNotEmpty();
    assertThat(result.get().getTitle()).isEqualTo("mt");
    
    // prepare
    inTransaction(() -> repository.update(messageThread.setDeleted(true)));

    // execute
    result = repository.findById(messageThread.getId());

    // verify
    assertThat(result).isEmpty();
  }

  @Test
  public void threadById_notFound() {
    assertThat(repository.findById(1L)).isEmpty();
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
    MessageThread storedThread = repository.findById(threadId).orElseThrow(RuntimeException::new);

    List<MessageThreadParty> parties = new ArrayList<>(originalThread.getParties());
    parties.add(new MessageThreadParty().setUser(user2));
    storedThread.setParties(parties);


    inTransaction(() -> repository.update(storedThread));


    MessageThread result = repository.findById(threadId).orElseThrow(RuntimeException::new);
    result.getParties().sort((p1,p2) -> p1.getId().compareTo(p2.getId()));
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
  public void deleteMessageThreadParty_threadId() {
    // prepare
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));
    
    MessageThread adMessageThread1 = inTransaction(() -> repository.create(new MessageThread().setAdId(id("ad1")).setGroup(false)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread1.getId()).setUser(user1)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(adMessageThread1.getId()).setUser(user2)));

    MessageThread groupMessageThread1 = inTransaction(() -> repository.create(new MessageThread().setAdId(null).setGroup(true)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(groupMessageThread1.getId()).setUser(user1)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(groupMessageThread1.getId()).setUser(user2)));

    MessageThread betweenUserMessageThread1 = inTransaction(() -> repository.create(new MessageThread().setAdId(null).setGroup(false)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(betweenUserMessageThread1.getId()).setUser(user1)));
    inTransaction(() -> em().persist(new MessageThreadParty().setMessageThreadId(betweenUserMessageThread1.getId()).setUser(user2)));
    
    // verify before execute
    assertThat(em().find(MessageThread.class, adMessageThread1.getId()).getParties().size()).isEqualTo(2);
    assertThat(em().find(MessageThread.class, groupMessageThread1.getId()).getParties().size()).isEqualTo(2);
    assertThat(em().find(MessageThread.class, betweenUserMessageThread1.getId()).getParties().size()).isEqualTo(2);
    
    // execute
    int result = inTransaction(() -> repository.deleteMessageThreadParty(user1, adMessageThread1.getId()));
    
    // verify
    assertThat(result).isEqualTo(1);
    assertThat(em().find(MessageThread.class, adMessageThread1.getId()).getParties().size()).isEqualTo(1);
    assertThat(em().find(MessageThread.class, groupMessageThread1.getId()).getParties().size()).isEqualTo(2);
    assertThat(em().find(MessageThread.class, betweenUserMessageThread1.getId()).getParties().size()).isEqualTo(2);

    // execute & verify
    result = inTransaction(() -> repository.deleteMessageThreadParty(user1, groupMessageThread1.getId()));
    assertThat(em().find(MessageThread.class, groupMessageThread1.getId()).getParties().size()).isEqualTo(1);

    // execute & verify
    result = inTransaction(() -> repository.deleteMessageThreadParty(user1, betweenUserMessageThread1.getId()));
    assertThat(em().find(MessageThread.class, betweenUserMessageThread1.getId()).getParties().size()).isEqualTo(1);
    
    // execute & verify
    result = inTransaction(() -> repository.deleteMessageThreadParty(user1, adMessageThread1.getId()));
    assertThat(result).isEqualTo(0);

    // execute & verify
    result = inTransaction(() -> repository.deleteMessageThreadParty(user2, adMessageThread1.getId()));
    assertThat(result).isEqualTo(1);
    assertThat(em().find(MessageThread.class, adMessageThread1.getId()).getParties().size()).isEqualTo(0);
  }
  
  @Test
  public void unreadMessageThreadCount() {
    Long communityId1 = inTransaction(() -> communityRepository.create(new Community().setName("community1")).getId());
    Long communityId2 = inTransaction(() -> communityRepository.create(new Community().setName("community2")).getId());
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));

    Long threadId1 = threadWithMessages(user, user2, communityId1, null).getId();
    Long threadId2 = threadWithMessages(user, user2, communityId1, now().minus(10, SECONDS)).getId();
    threadWithMessages(user, user2, communityId1, now().plus(10, SECONDS));
    threadWithoutMessages(user, user2, communityId1, null).getId();
    threadWithMessages(user, user2, communityId2, null).getId();
    threadWithMessages(user, user2, communityId2, now().minus(10, SECONDS)).getId();
    threadWithMessages(user, user2, communityId2, now().plus(10, SECONDS));

    List<Long> result = repository.unreadMessageThreadIds(user, communityId1);

    assertThat(result).containsExactly(threadId1, threadId2);
  }

  private MessageThread threadWithMessages(User myUser, User otherUser, Long communityId, Instant visitedAt) {
    MessageThreadParty myParty = new MessageThreadParty().setUser(myUser).setVisitedAt(visitedAt);
    MessageThreadParty counterParty = new MessageThreadParty().setUser(otherUser);
    MessageThread thread = new MessageThread().setCommunityId(communityId).setParties(asList(myParty, counterParty));
    inTransaction(() -> {
      repository.create(thread);
      messageRepository.create(new Message().setThreadId(thread.getId())).setCreatedBy(otherUser.getId());
      messageRepository.create(new Message().setThreadId(thread.getId())).setCreatedBy(myUser.getId());
    });

    return thread;
  }

  private MessageThread threadWithoutMessages(User myUser, User otherUser, Long communityId, Instant visitedAt) {
    MessageThreadParty myParty = new MessageThreadParty().setUser(myUser).setVisitedAt(visitedAt);
    MessageThreadParty counterParty = new MessageThreadParty().setUser(otherUser);
    MessageThread thread = new MessageThread().setCommunityId(communityId).setParties(asList(myParty, counterParty));
    inTransaction(() -> repository.create(thread));

    return thread;
  }
}