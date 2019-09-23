package commonsos.service;

import static commonsos.TestId.id;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import commonsos.command.app.GroupMessageThreadUpdateCommand;
import commonsos.command.app.MessagePostCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.exception.UserNotFoundException;
import commonsos.repository.AdRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;
import commonsos.view.app.MessageThreadView;
import commonsos.view.app.MessageView;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessageServiceTest {

  @Mock MessageThreadRepository messageThreadRepository;
  @Mock MessageRepository messageRepository;
  @Mock UserRepository userRepository;
  @Mock AdRepository adRepository;
  @InjectMocks @Spy MessageService service;
  @Captor ArgumentCaptor<MessageThread> messageThreadArgumentCaptor;
  @Captor ArgumentCaptor<MessageThread> messageThreadArgumentCaptor2;

  @BeforeEach
  public void setup() {
    when(userRepository.findStrictById(any())).thenCallRealMethod();
  }

  @Test
  public void thread() {
    User user = new User().setId(id("user id"));
    MessageThread messageThread = new MessageThread().setParties(asList(party(user)));
    when(messageThreadRepository.findById(id("thread-id"))).thenReturn(Optional.of(messageThread));
    MessageThreadView messageThreadView = new MessageThreadView();
    doReturn(messageThreadView).when(service).view(user, messageThread);

    assertThat(service.thread(user, id("thread-id"))).isSameAs(messageThreadView);
  }

  private MessageThreadParty party(User user) {
    return new MessageThreadParty().setUser(user);
  }

  @Test
  public void thread_canOnlyAccessThreadParticipatingIn() {
    MessageThread messageThread = new MessageThread().setParties(asList(party(new User().setId(id("other user")))));
    when(messageThreadRepository.findById(id("thread-id"))).thenReturn(Optional.of(messageThread));

    assertThrows(ForbiddenException.class, () -> service.thread(new User().setId(id("user id")), id("thread-id")));
    ;
  }

  @Test
  public void thread_notFound() {
    when(messageThreadRepository.findById(id("thread-id"))).thenReturn(empty());

    assertThrows(BadRequestException.class, () -> service.thread(new User().setId(id("user id")), id("thread-id")));
    ;
  }

  @Test
  public void messageThreadView() {
    User user = new User().setId(id("myself_mtv"));
    MessageThread messageThread = new MessageThread()
      .setCreatedBy(id("creator_mtv"))
      .setParties(asList(
          party(user),
          party(new User().setId(id("creator_mtv"))),
          party(new User().setId(id("other_mtv")))));

    MessageThreadView view = service.view(user, messageThread);

    assertThat(view.getParties().size()).isEqualTo(2);
    assertThat(view.getParties().get(0).getId()).isEqualTo(id("myself_mtv"));
    assertThat(view.getParties().get(1).getId()).isEqualTo(id("other_mtv"));
    assertThat(view.getCreator().getId()).isEqualTo(id("creator_mtv"));
    assertThat(view.getCounterParty().getId()).isEqualTo(id("creator_mtv"));
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
    when(messageThreadRepository.findById(id("thread id"))).thenReturn(Optional.of(new MessageThread().setParties(asList(party(user)))));
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
  }

  @Test
  public void view_group() {
    // prepare
    User user = new User().setId(id("user2_v_g"));
    MessageThread thread = new MessageThread().setGroup(true).setCreatedBy(id("user1_v_g")).setParties(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1_v_g"))),
        new MessageThreadParty().setUser(new User().setId(id("user2_v_g"))),
        new MessageThreadParty().setUser(new User().setId(id("user3_v_g")))
        ));

    when(messageRepository.lastMessage(any())).thenReturn(Optional.empty());

    // execute
    MessageThreadView result = service.view(user, thread);
    
    // verify
    assertThat(result.getAd()).isNull();
    
    assertThat(result.getParties().size()).isEqualTo(2);
    assertThat(result.getParties().get(0).getId()).isEqualTo(id("user2_v_g"));
    assertThat(result.getParties().get(1).getId()).isEqualTo(id("user3_v_g"));

    assertThat(result.getCreator().getId()).isEqualTo(id("user1_v_g"));
    
    assertThat(result.getCounterParty().getId()).isEqualTo(id("user1_v_g"));
  }

  @Test
  public void view_between_user() {
    // prepare
    User user = new User().setId(id("user2_v_b_u"));
    MessageThread thread = new MessageThread().setGroup(false).setCreatedBy(id("user1_v_b_u")).setParties(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1_v_b_u"))),
        new MessageThreadParty().setUser(new User().setId(id("user2_v_b_u")))
        ));

    when(messageRepository.lastMessage(any())).thenReturn(Optional.empty());

    // execute
    MessageThreadView result = service.view(user, thread);
    
    // verify
    assertThat(result.getAd()).isNull();
    
    assertThat(result.getParties().size()).isEqualTo(1);
    assertThat(result.getParties().get(0).getId()).isEqualTo(id("user2_v_b_u"));

    assertThat(result.getCreator().getId()).isEqualTo(id("user1_v_b_u"));
    
    assertThat(result.getCounterParty().getId()).isEqualTo(id("user1_v_b_u"));
  }

  @Test
  public void updateGroup_messagethread_not_found() {
    when(messageThreadRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(ForbiddenException.class, () -> service.updateGroup(new User(), new GroupMessageThreadUpdateCommand()));
  }

  @Test
  public void updateGroup_is_not_group() {
    MessageThread messageThread = new MessageThread().setGroup(false);
    when(messageThreadRepository.findById(any())).thenReturn(Optional.of(messageThread));

    assertThrows(BadRequestException.class, () -> service.updateGroup(new User(), new GroupMessageThreadUpdateCommand()));
  }

  @Test
  public void updateGroup_not_member() {
    MessageThread messageThread = new MessageThread().setGroup(true).setParties(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1")))
        ));
    when(messageThreadRepository.findById(any())).thenReturn(Optional.of(messageThread));
    assertThrows(ForbiddenException.class, () -> service.updateGroup(new User().setId(id("notMember")), new GroupMessageThreadUpdateCommand()));
  }

  @Test
  public void updateGroup() {
    // prepare
    MessageThread messageThread = new MessageThread().setGroup(true).setParties(new ArrayList<>(asList(
        new MessageThreadParty().setUser(new User().setId(id("user1_ug"))),
        new MessageThreadParty().setUser(new User().setId(id("user2_ug")))
        )));
    when(messageThreadRepository.findById(any())).thenReturn(Optional.of(messageThread));
    
    List<User> givenUsers = asList(
        new User().setId(id("user2_ug")),
        new User().setId(id("user3_ug"))
        );
    doReturn(givenUsers).when(service).validatePartiesCommunity(any(), any());
    doReturn(null).when(service).view(any(), any());
    
    // execute
    service.updateGroup(new User().setId(id("user1_ug")), new GroupMessageThreadUpdateCommand());
    
    // verify
    verify(messageThreadRepository).update(messageThreadArgumentCaptor.capture());
    MessageThread updated = messageThreadArgumentCaptor.getValue();
    assertThat(updated.getParties().size()).isEqualTo(3);
    assertThat(updated.getParties().get(0).getUser().getId()).isEqualTo(id("user1_ug"));
    assertThat(updated.getParties().get(1).getUser().getId()).isEqualTo(id("user2_ug"));
    assertThat(updated.getParties().get(2).getUser().getId()).isEqualTo(id("user3_ug"));
  }

  @Test
  public void validatePartiesCommunity_user_not_found() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> service.validatePartiesCommunity(asList(1L, 2L), id("community")));
  }

  @Test
  public void validatePartiesCommunity_requiresUser() {
    assertThrows(BadRequestException.class, () -> service.validatePartiesCommunity(asList(), id("community")));
  }

  @Test
  public void validatePartiesCommunity_not_a_member() {
    when(userRepository.findById(any())).thenReturn(Optional.of(
        new User().setCommunityUserList(asList(new CommunityUser().setCommunity(new Community().setId(id("community_1")))))));
    assertThrows(BadRequestException.class, () -> service.validatePartiesCommunity(asList(1L, 2L), id("community_2")));
  }

  @Test
  public void validatePartiesCommunity_valid() {
    when(userRepository.findById(any())).thenReturn(Optional.of(
        new User().setCommunityUserList(asList(new CommunityUser().setCommunity(new Community().setId(id("community")))))));
    service.validatePartiesCommunity(asList(1L, 2L), id("community"));
  }
}