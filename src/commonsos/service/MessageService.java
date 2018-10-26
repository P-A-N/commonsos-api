package commonsos.service;

import static java.lang.String.format;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;
import commonsos.service.command.CreateGroupCommand;
import commonsos.service.command.GroupMessageThreadUpdateCommand;
import commonsos.service.command.MessagePostCommand;
import commonsos.service.notification.PushNotificationService;
import commonsos.util.UserUtil;
import commonsos.view.AdView;
import commonsos.view.MessageThreadView;
import commonsos.view.MessageView;
import commonsos.view.UserView;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class MessageService {

  @Inject private MessageThreadRepository messageThreadRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private UserRepository userRepository;
  @Inject private AdService adService;
  @Inject PushNotificationService pushNotificationService;

  public MessageThreadView threadForAd(User user, Long adId) {
    MessageThread thread = messageThreadRepository.byAdId(user, adId).orElseGet(() -> createMessageThreadForAd(user, adId));
    return view(user, thread);
  }

  public MessageThreadView threadWithUser(User user, Long otherUserId) {
    MessageThread thread = messageThreadRepository.betweenUsers(user.getId(), otherUserId)
      .orElseGet(() -> createMessageThreadWithUser(user, otherUserId));
    return view(user, thread);
  }

  MessageThread createMessageThreadWithUser(User user, Long otherUserId) {
    User otherUser = userRepository.findStrictById(otherUserId);
    MessageThread messageThread = new MessageThread()
      .setCreatedBy(user.getId())
      .setCreatedAt(now())
      .setParties(asList(new MessageThreadParty().setUser(user), new MessageThreadParty().setUser(otherUser)));

    return messageThreadRepository.create(messageThread);
  }

  public MessageThreadView group(User user, CreateGroupCommand command) {
    List<User> users = validatePartiesCommunity(command.getMemberIds());
    List<MessageThreadParty> parties = usersToParties(users);
    parties.add(new MessageThreadParty().setUser(user));

    MessageThread messageThread = new MessageThread()
      .setGroup(true)
      .setTitle(command.getTitle())
      .setCreatedBy(user.getId())
      .setCreatedAt(now())
      .setParties(parties);
    return view(user, messageThreadRepository.create(messageThread));
  }

  private List<MessageThreadParty> usersToParties(List<User> users) {
    return users.stream().map(u -> new MessageThreadParty().setUser(u)).collect(toList());
  }

  public MessageThreadView updateGroup(User user, GroupMessageThreadUpdateCommand command) {
    MessageThread messageThread = messageThreadRepository.thread(command.getThreadId()).orElseThrow(ForbiddenException::new);
    if (!messageThread.isGroup()) throw new BadRequestException("Not a group message thread");
    if (!isUserAllowedToAccessMessageThread(user, messageThread)) throw new ForbiddenException("Not a thread member");

    List<User> existingUsers = messageThread.getParties().stream().map(MessageThreadParty::getUser).collect(toList());
    List<User> givenUsers = validatePartiesCommunity(command.getMemberIds());
    List<User> newUsers = givenUsers.stream()
      .filter(u -> !existingUsers.stream().anyMatch(eu -> eu.getId().equals(u.getId())))
      .collect(toList());

    List<MessageThreadParty> newParties = usersToParties(newUsers);
    messageThread.getParties().addAll(newParties);
    messageThread.setTitle(command.getTitle());
    messageThreadRepository.update(messageThread);

    return view(user, messageThread);
  }

  List<User> validatePartiesCommunity(List<Long> memberIds) {
    List<User> users = memberIds.stream().map(id -> userRepository.findStrictById(id)).collect(toList());
    if (users.isEmpty()) throw new BadRequestException("No group members specified");
    return users;
  }

  public MessageThreadView thread(User user, Long threadId) {
    return messageThreadRepository.thread(threadId)
      .map(t -> checkAccess(user, t))
      .map(t -> view(user, t))
      .orElseThrow(BadRequestException::new);
  }

  private MessageThread checkAccess(User user, MessageThread thread) {
    if (!isUserAllowedToAccessMessageThread(user, thread)) throw new ForbiddenException();
    return thread;
  }

  MessageThread createMessageThreadForAd(User user, Long adId) {
    Ad ad = adService.ad(adId);
    User adCreator = userRepository.findStrictById(ad.getCreatedBy());

    MessageThread messageThread = new MessageThread()
      .setCreatedBy(user.getId())
      .setCreatedAt(now())
      .setTitle(ad.getTitle()).setAdId(adId)
      .setParties(asList(new MessageThreadParty().setUser(adCreator), new MessageThreadParty().setUser(user)));

    return messageThreadRepository.create(messageThread);
  }

  public MessageThreadView view(User user, MessageThread thread) {
    List<UserView> parties = thread.getParties().stream()
      .filter(p -> !p.getUser().getId().equals(thread.getCreatedBy()))
      .map(MessageThreadParty::getUser)
      .map(UserUtil::view)
      .collect(toList());

    UserView creator = thread.getParties().stream()
      .filter(p -> p.getUser().getId().equals(thread.getCreatedBy()))
      .map(MessageThreadParty::getUser)
      .map(UserUtil::view).findFirst().orElseThrow(RuntimeException::new);

    UserView counterParty = concat(parties.stream(), of(creator))
      .filter(uv -> uv.getId() != user.getId())
      .findFirst()
      .orElseThrow(RuntimeException::new);

    AdView ad = thread.getAdId() == null ? null : adService.view(user, thread.getAdId());
    MessageView lastMessage = messageRepository.lastMessage(thread.getId()).map(this::view).orElse(null);

    return new MessageThreadView()
      .setId(thread.getId())
      .setAd(ad)
      .setTitle(thread.getTitle())
      .setLastMessage(lastMessage)
      .setCreatedAt(thread.getCreatedAt())
      .setGroup(thread.isGroup())
      .setCreator(creator)
      .setCounterParty(counterParty)
      .setParties(parties);
  }

  MessageView view(Message message) {
    return new MessageView()
      .setId(message.getId())
      .setCreatedAt(message.getCreatedAt())
      .setCreatedBy(message.getCreatedBy())
      .setText(message.getText());
  }

  public List<MessageThreadView> threads(User user) {
    List<Long> unreadMessageThreadIds = messageThreadRepository.unreadMessageThreadIds(user);
    List<MessageThreadView> threadViews = messageThreadRepository
      .listByUser(user)
      .stream()
      .map(thread -> view(user, thread))
      .filter(t -> t.getLastMessage() != null || t.isGroup())
      .map(p -> p.setUnread(unreadMessageThreadIds.contains(p.getId())))
      .collect(toList());
    return sortAsNewestFirst(threadViews);
  }

  List<MessageThreadView> sortAsNewestFirst(List<MessageThreadView> threadViews) {
    threadViews.sort(comparing((MessageThreadView t) -> {
      if (t.getLastMessage() == null) return t.getCreatedAt();
      return t.getLastMessage().getCreatedAt();
    }).reversed());
    return threadViews;
  }

  public MessageView postMessage(User user, MessagePostCommand command) {
    MessageThread messageThread = messageThreadRepository.thread(command.getThreadId()).map(thread -> checkAccess(user, thread)).get();
    Message message = messageRepository.create(new Message()
      .setCreatedBy(user.getId())
      .setCreatedAt(now())
      .setThreadId(command.getThreadId())
      .setText(command.getText()));

    notifyThreadParties(user, messageThread, message);

    return view(message);
  }

    private void notifyThreadParties(User senderUser, MessageThread messageThread, Message message) {
    messageThread.getParties().stream()
      .filter(p -> !p.getUser().equals(senderUser))
      .forEach(p -> {
        String messageText = format("%s:\n\n%s", senderUser.getUsername(), message.getText());
        Map<String, String> params = ImmutableMap.of(
          "type", "new_message",
          "threadId", Long.toString(messageThread.getId()));
        pushNotificationService.send(p.getUser(), messageText, params);
      });
  }

  public List<MessageView> messages(User user, Long threadId) {
    MessageThread thread = messageThreadRepository.thread(threadId).orElseThrow(BadRequestException::new);
    if (!isUserAllowedToAccessMessageThread(user, thread)) throw new ForbiddenException();

    markVisited(user, thread);

    return messageRepository.listByThread(threadId).stream().map(this::view).collect(toList());
  }

  private void markVisited(User user, MessageThread thread) {
    MessageThreadParty me = thread.getParties().stream().filter(p -> p.getUser().equals(user)).findFirst().orElseThrow(RuntimeException::new);
    me.setVisitedAt(now());
    messageThreadRepository.update(me);
  }

  private boolean isUserAllowedToAccessMessageThread(User user, MessageThread thread) {
    return thread.getParties().stream().anyMatch(p -> p.getUser().equals(user));
  }

  public int unreadMessageThreadCount(User user) {
    return messageThreadRepository.unreadMessageThreadIds(user).size();
  }
}
