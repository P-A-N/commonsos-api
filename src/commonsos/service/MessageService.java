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
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;

import commonsos.controller.command.PaginationCommand;
import commonsos.controller.command.app.CreateDirectMessageThreadCommand;
import commonsos.controller.command.app.CreateGroupCommand;
import commonsos.controller.command.app.GroupMessageThreadUpdateCommand;
import commonsos.controller.command.app.MessagePostCommand;
import commonsos.controller.command.app.MessageThreadListCommand;
import commonsos.controller.command.app.UpdateMessageThreadPersonalTitleCommand;
import commonsos.controller.command.app.UploadPhotoCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;
import commonsos.service.notification.PushNotificationService;
import commonsos.util.AdUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.UserUtil;
import commonsos.view.app.AdView;
import commonsos.view.app.MessageListView;
import commonsos.view.app.MessageThreadListView;
import commonsos.view.app.MessageThreadView;
import commonsos.view.app.MessageView;
import commonsos.view.app.PublicUserView;

@Singleton
public class MessageService {

  @Inject private MessageThreadRepository messageThreadRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private UserRepository userRepository;
  @Inject private AdRepository adRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private DeleteService deleteService;
  @Inject private PushNotificationService pushNotificationService;
  @Inject private ImageUploadService imageService;

  public MessageThreadView threadForAd(User user, Long adId) {
    MessageThread thread = messageThreadRepository.byCreaterAndAdId(user.getId(), adId).orElseGet(() -> createMessageThreadForAd(user, adId));
    return view(user, thread);
  }

  private MessageThread createMessageThreadForAd(User user, Long adId) {
    Ad ad = adRepository.findStrict(adId);
    User adCreator = userRepository.findStrictById(ad.getCreatedBy());

    MessageThread messageThread = new MessageThread()
      .setCommunityId(ad.getCommunityId())
      .setCreatedBy(user.getId())
      .setTitle(ad.getTitle()).setAdId(adId)
      .setParties(asList(new MessageThreadParty().setUser(adCreator), new MessageThreadParty().setUser(user)));

    return messageThreadRepository.createForAdIfNotExists(messageThread);
  }

  public MessageThreadView threadWithUser(User user, CreateDirectMessageThreadCommand command) {
    MessageThread thread = messageThreadRepository.betweenUsers(user.getId(), command.getOtherUserId(), command.getCommunityId())
      .orElseGet(() -> createMessageThreadWithUser(user, command));
    return view(user, thread);
  }

  private MessageThread createMessageThreadWithUser(User user, CreateDirectMessageThreadCommand command) {
    User otherUser = userRepository.findStrictById(command.getOtherUserId());
    if (!UserUtil.isMember(user, command.getCommunityId()) || !UserUtil.isMember(otherUser, command.getCommunityId())) throw new BadRequestException(String.format("User isn't a member of the community. [communityId=%d]", command.getCommunityId()));
    
    MessageThread messageThread = new MessageThread()
      .setCommunityId(command.getCommunityId())
      .setCreatedBy(user.getId())
      .setParties(asList(new MessageThreadParty().setUser(user), new MessageThreadParty().setUser(otherUser)));

    return messageThreadRepository.createForBetweenUserIfNotExists(messageThread);
  }

  public MessageThreadView group(User user, CreateGroupCommand command) {
    communityRepository.findPublicStrictById(command.getCommunityId());
    if (!UserUtil.isMember(user, command.getCommunityId())) throw new BadRequestException("User isn't a member of the community");
    List<User> users = validatePartiesCommunity(command.getMemberIds(), command.getCommunityId());
    List<MessageThreadParty> parties = usersToParties(users);
    parties.add(new MessageThreadParty().setUser(user));

    MessageThread messageThread = new MessageThread()
      .setGroup(true)
      .setCommunityId(command.getCommunityId())
      .setTitle(command.getTitle())
      .setCreatedBy(user.getId())
      .setParties(parties);
    return view(user, messageThreadRepository.create(messageThread));
  }

  private List<MessageThreadParty> usersToParties(List<User> users) {
    return users.stream().map(u -> new MessageThreadParty().setUser(u)).collect(toList());
  }

  public MessageThreadView updateGroup(User user, GroupMessageThreadUpdateCommand command) {
    MessageThread messageThread = messageThreadRepository.findById(command.getThreadId()).orElseThrow(ForbiddenException::new);
    if (!messageThread.isGroup()) throw new BadRequestException("Not a group message thread");
    if (!isUserAllowedToAccessMessageThread(user, messageThread)) throw new ForbiddenException("Not a thread member");

    List<User> existingUsers = messageThread.getParties().stream().map(MessageThreadParty::getUser).collect(toList());
    List<User> givenUsers = validatePartiesCommunity(command.getMemberIds(), messageThread.getCommunityId());
    List<User> newUsers = givenUsers.stream()
      .filter(u -> !existingUsers.stream().anyMatch(eu -> eu.getId().equals(u.getId())))
      .collect(toList());

    List<MessageThreadParty> newParties = usersToParties(newUsers);
    messageThreadRepository.lockForUpdate(messageThread);
    messageThread.getParties().addAll(newParties);
    messageThread.setTitle(command.getTitle());
    messageThreadRepository.update(messageThread);

    return view(user, messageThread);
  }

  List<User> validatePartiesCommunity(List<Long> memberIds, Long communityId) {
    List<User> users = memberIds.stream().map(id -> userRepository.findStrictById(id)).collect(toList());
    users.forEach(u -> {
      if (!UserUtil.isMember(u, communityId)) throw new BadRequestException(String.format("User(id=%d) isn't a member of the community", u.getId()));
    });
    if (users.isEmpty()) throw new BadRequestException("No group members specified");
    return users;
  }

  public MessageThreadView thread(User user, Long threadId) {
    return messageThreadRepository.findById(threadId)
      .map(t -> checkAccess(user, t))
      .map(t -> view(user, t))
      .orElseThrow(BadRequestException::new);
  }

  private MessageThread checkAccess(User user, MessageThread thread) {
    if (!isUserAllowedToAccessMessageThread(user, thread)) throw new ForbiddenException();
    return thread;
  }

  public MessageThreadView view(User user, MessageThread thread) {
    MessageThreadParty userMtp = thread.getParties().stream().filter(p -> p.getUser().getId().equals(user.getId())).findFirst().get();
    
    List<PublicUserView> parties = thread.getParties().stream()
      .filter(p -> !p.getUser().getId().equals(thread.getCreatedBy()))
      .map(MessageThreadParty::getUser)
      .map(UserUtil::publicView)
      .sorted((p1,p2) -> p1.getId().compareTo(p2.getId()))
      .collect(toList());

    PublicUserView creator = thread.getParties().stream()
      .filter(p -> p.getUser().getId().equals(thread.getCreatedBy()))
      .map(MessageThreadParty::getUser)
      .map(UserUtil::publicView).findFirst().orElse(null);

    PublicUserView counterParty = concat(parties.stream(), of(creator))
      .filter(uv -> uv != null && uv.getId() != user.getId())
      .sorted((p1,p2) -> p1.getId().compareTo(p2.getId()))
      .findFirst().orElse(null);

    AdView adView = null;
    if (thread.getAdId() != null) {
      Ad ad = adRepository.findStrict(thread.getAdId());
      User createdBy = userRepository.findStrictById(ad.getCreatedBy());
      adView = AdUtil.view(ad, createdBy, user);
    }
    MessageView lastMessage = messageRepository.lastMessage(thread.getId()).map(this::view).orElse(null);

    return new MessageThreadView()
      .setId(thread.getId())
      .setAd(adView)
      .setCommunityId(thread.getCommunityId())
      .setTitle(thread.getTitle())
      .setPersonalTitle(userMtp.getPersonalTitle())
      .setLastMessage(lastMessage)
      .setCreatedAt(thread.getCreatedAt())
      .setGroup(thread.isGroup())
      .setCreator(creator)
      .setCounterParty(counterParty)
      .setParties(parties)
      .setPhotoUrl(userMtp.getPhotoUrl());
  }

  MessageView view(Message message) {
    return new MessageView()
      .setId(message.getId())
      .setCreatedAt(message.getCreatedAt())
      .setCreatedBy(message.getCreatedBy())
      .setText(message.getText());
  }

  public MessageThreadListView searchThreads(User user, MessageThreadListCommand command, PaginationCommand pagination) {
    communityRepository.findPublicStrictById(command.getCommunityId());
    
    List<Long> unreadMessageThreadIds = messageThreadRepository.unreadMessageThreadIds(user, command.getCommunityId());
    
    ResultList<MessageThread> result = messageThreadRepository.listByUser(user, command.getCommunityId(), command.getMemberFilter(), command.getMessageFilter(), null);
    
    List<MessageThreadView> threadViews = result.getList().stream()
      .map(thread -> view(user, thread))
      .filter(t -> t.getLastMessage() != null || t.isGroup())
      .map(p -> p.setUnread(unreadMessageThreadIds.contains(p.getId())))
      .collect(toList());
    threadViews = sortAsNewestFirst(threadViews);

    MessageThreadListView listView = new MessageThreadListView();
    listView.setPagination(PaginationUtil.toView(threadViews, pagination));
    List<MessageThreadView> paginationedViews = PaginationUtil.pagination(threadViews, pagination);
    listView.setMessageThreadList(paginationedViews);
    
    return listView;
  }

  List<MessageThreadView> sortAsNewestFirst(List<MessageThreadView> threadViews) {
    threadViews.sort(comparing((MessageThreadView t) -> {
      if (t.getLastMessage() == null) return t.getCreatedAt();
      return t.getLastMessage().getCreatedAt();
    }).reversed());
    
    return threadViews;
  }

  public MessageView postMessage(User user, MessagePostCommand command) {
    MessageThread messageThread = messageThreadRepository.findById(command.getThreadId()).map(thread -> checkAccess(user, thread)).get();
    Message message = messageRepository.create(new Message()
      .setCreatedBy(user.getId())
      .setThreadId(command.getThreadId())
      .setText(command.getText()));

    notifyThreadParties(user, messageThread, message);

    return view(message);
  }

    private void notifyThreadParties(User senderUser, MessageThread messageThread, Message message) {
    messageThread.getParties().stream()
      .filter(p -> !p.getUser().getId().equals(senderUser.getId()))
      .forEach(p -> {
        String messageText = format("%s:\n\n%s", senderUser.getUsername(), message.getText());
        Map<String, String> params = ImmutableMap.of(
          "type", "new_message",
          "threadId", Long.toString(messageThread.getId()));
        pushNotificationService.send(p.getUser(), messageText, params);
      });
  }

  public MessageListView messages(User user, Long threadId, PaginationCommand pagination) {
    MessageThread thread = messageThreadRepository.findStrictById(threadId);
    if (!isUserAllowedToAccessMessageThread(user, thread)) throw new ForbiddenException();

    markVisited(user, thread);

    ResultList<Message> result = messageRepository.listByThread(threadId, pagination);

    MessageListView listView = new MessageListView();
    listView.setMessageList(result.getList().stream().map(this::view).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public MessageThreadView updatePersonalTitle(User user, UpdateMessageThreadPersonalTitleCommand command) {
    // verify
    MessageThread messageThread = messageThreadRepository.findStrictById(command.getThreadId());
    Optional<MessageThreadParty> userMtp = messageThread.getParties().stream().filter(p -> p.getUser().getId().equals(user.getId())).findFirst();
    if (!userMtp.isPresent()) throw new BadRequestException("User is not a member of thread");

    if (!messageThread.isGroup()) throw new BadRequestException("Only group thread is allowed.");
    
    // updatePersonalTitle
    messageThreadRepository.lockForUpdate(messageThread);
    userMtp.get().setPersonalTitle(command.getPersonalTitle());
    messageThreadRepository.update(messageThread);
    return view(user, messageThread);
  }

  public String updatePhoto(User user, UploadPhotoCommand command, Long threadId) {
    // verify
    MessageThread thread = messageThreadRepository.findStrictById(threadId);
    Optional<MessageThreadParty> userMtp = thread.getParties().stream().filter(p -> p.getUser().getId().equals(user.getId())).findFirst();
    if (!userMtp.isPresent()) throw new BadRequestException("User is not a member of thread");

    if (!thread.isGroup()) throw new BadRequestException("Only group thread is allowed.");

    // updatePhoto
    String url = imageService.create(command, "");
    imageService.delete(userMtp.get().getPhotoUrl());
    
    messageThreadRepository.lockForUpdate(thread);
    userMtp.get().setPhotoUrl(url);
    messageThreadRepository.update(thread);
    return url;
  }

  public void unsubscribe(User user, Long threadId) {
    // verify
    MessageThread thread = messageThreadRepository.findStrictById(threadId);
    if (!thread.isGroup()) throw new BadRequestException("Only group thread is allowed.");

    deleteService.deleteMessageThreadParty(user, threadId);
  }

  private void markVisited(User user, MessageThread thread) {
    MessageThreadParty me = thread.getParties().stream().filter(p -> p.getUser().getId().equals(user.getId())).findFirst().orElseThrow(RuntimeException::new);
    messageThreadRepository.lockForUpdate(me);
    me.setVisitedAt(now());
    messageThreadRepository.update(me);
  }

  private boolean isUserAllowedToAccessMessageThread(User user, MessageThread thread) {
    return thread.getParties().stream().anyMatch(p -> p.getUser().getId().equals(user.getId()));
  }

  public int unreadMessageThreadCount(User user, Long communityId) {
    communityRepository.findPublicStrictById(communityId);
    return messageThreadRepository.unreadMessageThreadIds(user, communityId).size();
  }
}
