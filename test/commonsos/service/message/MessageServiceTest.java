package commonsos.service.message;

import static commonsos.TestId.id;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.ForbiddenException;
import commonsos.exception.BadRequestException;
import commonsos.exception.UserNotFoundException;
import commonsos.repository.ad.Ad;
import commonsos.repository.message.Message;
import commonsos.repository.message.MessageRepository;
import commonsos.repository.message.MessageThread;
import commonsos.repository.message.MessageThreadParty;
import commonsos.repository.message.MessageThreadRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.PushNotificationService;
import commonsos.service.ad.AdService;
import commonsos.service.ad.AdView;
import commonsos.service.user.UserView;
import commonsos.util.UserUtil;

@RunWith(MockitoJUnitRunner.class)

public class MessageServiceTest {

  @Mock MessageThreadRepository messageThreadRepository;
  @Mock MessageRepository messageRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserUtil userUtil;
  @Mock AdService adService;
  @Mock PushNotificationService pushNotificationService;
  @InjectMocks @Spy MessageService service;
  @Captor ArgumentCaptor<MessageThread> messageThreadArgumentCaptor;
  @Captor ArgumentCaptor<MessageThread> messageThreadArgumentCaptor2;

  @Test
  public void threadForAd_findExisting() {
    MessageThread messageThread = new MessageThread();
    User user = new User();
    when(messageThreadRepository.byAdId(user, id("ad-id"))).thenReturn(Optional.of(messageThread));
    MessageThreadView messageThreadView = new MessageThreadView();
    doReturn(messageThreadView).when(service).view(user, messageThread);

    MessageThreadView result = service.threadForAd(user, id("ad-id"));

    assertThat(result).isSameAs(messageThreadView);
  }

  @Test
  public void threadForAd_createNewIfNotPresent() {
    User user = new User().setId(id("user id"));
    when(messageThreadRepository.byAdId(user, id("ad-id"))).thenReturn(empty());

    MessageThread newThread = new MessageThread();
    doReturn(newThread).when(service).createMessageThreadForAd(user, id("ad-id"));

    MessageThreadView messageThreadView = new MessageThreadView();
    doReturn(messageThreadView).when(service).view(user, newThread);


    MessageThreadView result = service.threadForAd(user, id("ad-id"));


    assertThat(result).isEqualTo(messageThreadView);
  }

  @Test
  public void threadWithUser_existingThread() {
    User user = new User().setId(id("my id"));
    MessageThread existingThread = new MessageThread();
    when(messageThreadRepository.betweenUsers(id("my id"), id("other user id"))).thenReturn(Optional.of(existingThread));
    MessageThreadView messageThreadView = new MessageThreadView();
    doReturn(messageThreadView).when(service).view(user, existingThread);

    MessageThreadView result = service.threadWithUser(user, id("other user id"));

    assertThat(result).isSameAs(messageThreadView);
  }

  @Test
  public void threadWithUser_createNew() {
    User user = new User().setId(id("my id"));
    when(messageThreadRepository.betweenUsers(id("my id"), id("other user id"))).thenReturn(empty());
    MessageThread createdThread = new MessageThread();
    doReturn(createdThread).when(service).createMessageThreadWithUser(user, id("other user id"));
    MessageThreadView messageThreadView = new MessageThreadView();
    doReturn(messageThreadView).when(service).view(user, createdThread);

    MessageThreadView result = service.threadWithUser(user, id("other user id"));

    assertThat(result).isSameAs(messageThreadView);
  }

  @Test
  public void createMessageThreadWithUser() {
    User user = new User().setId(id("user id"));
    User counterparty = new User().setId(id("counterparty id"));
    MessageThread newThread = new MessageThread();
    when(messageThreadRepository.create(messageThreadArgumentCaptor.capture())).thenReturn(newThread);
    when(userRepository.findById(id("counterparty id"))).thenReturn(Optional.of(counterparty));

    MessageThread result = service.createMessageThreadWithUser(user, id("counterparty id"));

    assertThat(result).isEqualTo(newThread);
    MessageThread createdThread = messageThreadArgumentCaptor.getValue();
    assertThat(createdThread.getCreatedBy()).isEqualTo(id("user id"));
    assertThat(createdThread.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
    assertThat(createdThread.isGroup()).isFalse();
    assertThat(createdThread.getParties()).extracting("user").containsExactly(user, counterparty);
  }

  @Test
  public void thread() {
    User user = new User().setId(id("user id"));
    MessageThread messageThread = new MessageThread().setParties(asList(party(user)));
    when(messageThreadRepository.thread(id("thread-id"))).thenReturn(Optional.of(messageThread));
    MessageThreadView messageThreadView = new MessageThreadView();
    doReturn(messageThreadView).when(service).view(user, messageThread);

    assertThat(service.thread(user, id("thread-id"))).isSameAs(messageThreadView);
  }

  private MessageThreadParty party(User user) {
    return new MessageThreadParty().setUser(user);
  }

  @Test(expected = ForbiddenException.class)
  public void thread_canOnlyAccessThreadParticipatingIn() {
    MessageThread messageThread = new MessageThread().setParties(asList(party(new User().setId(id("other user")))));
    when(messageThreadRepository.thread(id("thread-id"))).thenReturn(Optional.of(messageThread));

    service.thread(new User().setId(id("user id")), id("thread-id"));
  }

  @Test(expected = BadRequestException.class)
  public void thread_notFound() {
    when(messageThreadRepository.thread(id("thread-id"))).thenReturn(empty());

    service.thread(new User().setId(id("user id")), id("thread-id"));
  }

  @Test
  public void createMessageThreadForAd() {
    User user = new User().setId(id("user id"));
    User counterparty = new User().setId(id("counterparty id"));
    when(adService.ad(id("ad-id"))).thenReturn(new Ad().setTitle("Title").setCreatedBy(id("ad publisher")));
    MessageThread newThread = new MessageThread();
    when(messageThreadRepository.create(messageThreadArgumentCaptor.capture())).thenReturn(newThread);
    when(userRepository.findById(id("ad publisher"))).thenReturn(Optional.of(counterparty));

    MessageThread result = service.createMessageThreadForAd(user, id("ad-id"));

    assertThat(result).isEqualTo(newThread);
    MessageThread createdThread = messageThreadArgumentCaptor.getValue();
    assertThat(createdThread.getAdId()).isEqualTo(id("ad-id"));
    assertThat(createdThread.getCreatedBy()).isEqualTo(id("user id"));
    assertThat(createdThread.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
    assertThat(createdThread.isGroup()).isFalse();
    assertThat(createdThread.getParties()).extracting("user").containsExactly(counterparty, user);
  }

  @Test
  public void messageThreadView() {
    User user = new User().setId(id("myself"));
    User counterparty = new User().setId(id("counterparty"));
    Message message = new Message().setId(33L);
    Instant now = now();
    MessageThread messageThread = new MessageThread()
      .setId(id("thread id"))
      .setTitle("title")
      .setAdId(id("ad id"))
      .setGroup(true)
      .setCreatedAt(now)
      .setCreatedBy(user.getId())
      .setParties(asList(party(user), party(counterparty)));
    UserView conterpartyView = new UserView();
    when(userUtil.view(counterparty)).thenReturn(conterpartyView);
    UserView userView = new UserView();
    when(userUtil.view(user)).thenReturn(userView);
    MessageView messageView = new MessageView();
    doReturn(messageView).when(service).view(message);
    when(messageRepository.lastMessage(id("thread id"))).thenReturn(Optional.of(message));
    AdView adView = new AdView();
    when(adService.view(user, id("ad id"))).thenReturn(adView);

    MessageThreadView view = service.view(user, messageThread);

    assertThat(view.getId()).isEqualTo(id("thread id"));
    assertThat(view.getAd()).isEqualTo(adView);
    assertThat(view.getTitle()).isEqualTo("title");
    assertThat(view.getParties()).containsExactly(conterpartyView);
    assertThat(view.getLastMessage()).isEqualTo(messageView);
    assertThat(view.isUnread()).isEqualTo(false);
    assertThat(view.isGroup()).isEqualTo(true);
    assertThat(view.getCreatedAt()).isEqualTo(now);
    assertThat(view.getCreator()).isEqualTo(userView);
    assertThat(view.getCounterParty()).isEqualTo(conterpartyView);
  }

  @Test
  public void messageView() {
    Instant messageCreated = now();
    Message message = new Message()
      .setId(33L)
      .setThreadId(id("thread id"))
      .setCreatedAt(messageCreated)
      .setCreatedBy(id("user id"))
      .setText("hello");

    MessageView result = service.view(message);

    assertThat(result).isEqualTo(new MessageView()
      .setId(33L)
      .setCreatedAt(messageCreated)
      .setCreatedBy(id("user id"))
      .setText("hello"));
  }

  @Test
  public void threads() {
    User user = new User();
    MessageThread thread = new MessageThread().setId(id("unread"));
    when(messageThreadRepository.listByUser(user)).thenReturn(asList(thread));
    MessageThreadView view = new MessageThreadView().setId(id("unread")).setLastMessage(new MessageView());
    doReturn(view).when(service).view(user, thread);
    when(messageThreadRepository.unreadMessageThreadIds(user)).thenReturn(asList(id("unread")));

    List<MessageThreadView> result = service.threads(user);

    assertThat(result).containsExactly(view);
    assertThat(view.isUnread()).isTrue();
  }

  @Test
  public void threads_excludePrivateThreadsWithoutMessages() {
    User user = new User();
    MessageThread thread = new MessageThread();
    when(messageThreadRepository.listByUser(user)).thenReturn(asList(thread));
    MessageThreadView threadView = new MessageThreadView();
    doReturn(threadView).when(service).view(user, thread);

    assertThat(service.threads(user)).isEmpty();
  }

  @Test
  public void threads_includesGroupThreadsWithoutMessages() {
    User user = new User();
    MessageThread thread = new MessageThread().setGroup(true);
    when(messageThreadRepository.listByUser(user)).thenReturn(asList(thread));
    MessageThreadView threadView = new MessageThreadView().setGroup(true);
    doReturn(threadView).when(service).view(user, thread);

    assertThat(service.threads(user)).isNotEmpty();
  }

  @Test
  public void ordersNewestThreadsFirst() {
    MessageThreadView view1 = new MessageThreadView().setLastMessage(new MessageView().setCreatedAt(now().minus(2, HOURS)));
    MessageThreadView view2 = new MessageThreadView().setLastMessage(new MessageView().setCreatedAt(now().minus(1, HOURS)));
    MessageThreadView view3 = new MessageThreadView().setLastMessage(new MessageView().setCreatedAt(now().minus(3, HOURS)));
    List<MessageThreadView> data = new ArrayList<>(asList(view1, view2, view3));

    service.sortAsNewestFirst(data);

    assertThat(data).containsExactly(view2, view1, view3);
  }

  @Test
  public void ordersNewestThreadsFirst_usesCreatedAtForGroupsWithoutMessages() {
    MessageThreadView view1 = new MessageThreadView().setLastMessage(new MessageView().setCreatedAt(now().minus(2, HOURS)));
    MessageThreadView view2 = new MessageThreadView().setCreatedAt(now().minus(1, HOURS)).setGroup(true);
    MessageThreadView view3 = new MessageThreadView().setLastMessage(new MessageView().setCreatedAt(now().minus(3, HOURS)));
    List<MessageThreadView> data = new ArrayList<>(asList(view1, view2, view3));

    service.sortAsNewestFirst(data);

    assertThat(data).containsExactly(view2, view1, view3);
  }

  @Test
  public void postMessage() {
    User user = new User().setId(id("user id"));
    when(messageThreadRepository.thread(id("thread id"))).thenReturn(Optional.of(new MessageThread().setParties(asList(party(user)))));
    Message createdMessage = new Message();
    when(messageRepository.create(any())).thenReturn(createdMessage);
    MessageView messageView = new MessageView();
    doReturn(messageView).when(service).view(createdMessage);

    MessageView result = service.postMessage(user, new MessagePostCommand().setThreadId(id("thread id")).setText("message text"));

    assertThat(result).isSameAs(messageView);
    ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository).create(messageArgument.capture());
    Message message = messageArgument.getValue();
    assertThat(message.getThreadId()).isEqualTo(id("thread id"));
    assertThat(message.getCreatedBy()).isEqualTo(id("user id"));
    assertThat(message.getText()).isEqualTo("message text");
    assertThat(message.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
  }

  @Test
  public void messages() {
    Message message = new Message();
    when(messageRepository.listByThread(id("thread id"))).thenReturn(asList(message));
    MessageView view = new MessageView();
    doReturn(view).when(service).view(message);
    MessageThreadParty party = party(new User().setId(id("user id")));
    when(messageThreadRepository.thread((id("thread id")))).thenReturn(Optional.of(new MessageThread().setParties(asList(party))));

    List<MessageView> result = service.messages(new User().setId(id("user id")), id("thread id"));

    assertThat(result).containsExactly(view);
    verify(messageThreadRepository).update(party);
    assertThat(party.getVisitedAt()).isCloseTo(now(), within(1, SECONDS));
  }

  @Test(expected = BadRequestException.class)
  public void message_threadMustExist() {
    when(messageThreadRepository.thread((id("thread id")))).thenReturn(empty());

    service.messages(new User().setId(id("me")), id("thread id"));
  }

  @Test(expected = ForbiddenException.class)
  public void message_canBeAccessedByParticipatingParty() {
    MessageThread messageThread = new MessageThread().setParties(asList(party(new User().setId(id("other user")))));
    when(messageThreadRepository.thread((id("thread id")))).thenReturn(Optional.of(messageThread));

    service.messages(new User().setId(id("me")), id("thread id"));
  }

  @Test
  public void unreadMessageThreadCount() {
    User user = new User();
    when(messageThreadRepository.unreadMessageThreadIds(user)).thenReturn(asList(1L, 2L, 3L));

    int result = service.unreadMessageThreadCount(user);

    assertThat(result).isEqualTo(3);
  }

  @Test
  public void view_group() {
    // prepare
    User user = new User().setId(id("user2"));
    MessageThread thread = new MessageThread().setGroup(true).setCreatedBy(id("user1")).setParties(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1"))),
        new MessageThreadParty().setUser(new User().setId(id("user2"))),
        new MessageThreadParty().setUser(new User().setId(id("user3")))
        ));

    when(userUtil.view(any())).thenCallRealMethod();
    when(messageRepository.lastMessage(any())).thenReturn(Optional.empty());

    // execute
    MessageThreadView result = service.view(user, thread);
    
    // verify
    assertThat(result.getAd()).isNull();
    
    assertThat(result.getParties().size()).isEqualTo(2);
    assertThat(result.getParties().get(0).getId()).isEqualTo(id("user2"));
    assertThat(result.getParties().get(1).getId()).isEqualTo(id("user3"));

    assertThat(result.getCreator().getId()).isEqualTo(id("user1"));
    
    assertThat(result.getCounterParty().getId()).isEqualTo(id("user3"));
  }

  @Test
  public void view_ad_thread() {
    // prepare
    User user = new User().setId(id("user2"));
    MessageThread thread = new MessageThread().setId(id("thread")).setAdId(id("ad")).setCreatedBy(id("user1")).setParties(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1"))),
        new MessageThreadParty().setUser(new User().setId(id("user2"))),
        new MessageThreadParty().setUser(new User().setId(id("user3")))
        ));
    when(userUtil.view(any())).thenCallRealMethod();
    when(messageRepository.lastMessage(any())).thenReturn(Optional.empty());
    when(adService.view(any(User.class), any(Long.class))).thenReturn(new AdView().setId(id("ad")));
    
    // execute
    MessageThreadView result = service.view(user, thread);
    
    // verify
    assertThat(result.getAd().getId()).isEqualTo(id("ad"));
  }

  @Test
  public void view_between_user() {
    // prepare
    User user = new User().setId(id("user2"));
    MessageThread thread = new MessageThread().setGroup(false).setCreatedBy(id("user1")).setParties(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1"))),
        new MessageThreadParty().setUser(new User().setId(id("user2")))
        ));

    when(userUtil.view(any())).thenCallRealMethod();
    when(messageRepository.lastMessage(any())).thenReturn(Optional.empty());

    // execute
    MessageThreadView result = service.view(user, thread);
    
    // verify
    assertThat(result.getAd()).isNull();
    
    assertThat(result.getParties().size()).isEqualTo(1);
    assertThat(result.getParties().get(0).getId()).isEqualTo(id("user2"));

    assertThat(result.getCreator().getId()).isEqualTo(id("user1"));
    
    assertThat(result.getCounterParty().getId()).isEqualTo(id("user1"));
  }

  @Test(expected = ForbiddenException.class)
  public void updateGroup_messagethread_not_found() {
    when(messageThreadRepository.thread(any())).thenReturn(Optional.empty());
    service.updateGroup(new User(), new GroupMessageThreadUpdateCommand());
  }

  @Test(expected = BadRequestException.class)
  public void updateGroup_is_not_group() {
    MessageThread messageThread = new MessageThread().setGroup(false);
    when(messageThreadRepository.thread(any())).thenReturn(Optional.of(messageThread));
    service.updateGroup(new User(), new GroupMessageThreadUpdateCommand());
  }

  @Test(expected = ForbiddenException.class)
  public void updateGroup_not_member() {
    MessageThread messageThread = new MessageThread().setGroup(true).setParties(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1")))
        ));
    when(messageThreadRepository.thread(any())).thenReturn(Optional.of(messageThread));
    service.updateGroup(new User().setId(id("notMember")), new GroupMessageThreadUpdateCommand());
  }

  @Test
  public void updateGroup() {
    // prepare
    MessageThread messageThread = new MessageThread().setGroup(true).setParties(new ArrayList<>(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1"))),
        new MessageThreadParty().setUser(new User().setId(id("user2")))
        )));
    when(messageThreadRepository.thread(any())).thenReturn(Optional.of(messageThread));
    
    List<User> givenUsers = asList(
        new User().setId(id("user2")),
        new User().setId(id("user3"))
        );
    doReturn(givenUsers).when(service).validatePartiesCommunity(any());
    doReturn(null).when(service).view(any(), any());
    
    // execute
    service.updateGroup(new User().setId(id("user1")), new GroupMessageThreadUpdateCommand());
    
    // verify
    verify(messageThreadRepository).update(messageThreadArgumentCaptor.capture());
    MessageThread updated = messageThreadArgumentCaptor.getValue();
    assertThat(updated.getParties().size()).isEqualTo(3);
    assertThat(updated.getParties().get(0).getUser().getId()).isEqualTo(id("user1"));
    assertThat(updated.getParties().get(1).getUser().getId()).isEqualTo(id("user2"));
    assertThat(updated.getParties().get(2).getUser().getId()).isEqualTo(id("user3"));
  }

  @Test(expected = UserNotFoundException.class)
  public void validatePartiesCommunity_user_not_found() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    service.validatePartiesCommunity(asList(1L, 2L));
  }

  @Test(expected = BadRequestException.class)
  public void validatePartiesCommunity_requiresUser() {
    service.validatePartiesCommunity(asList());
  }
}