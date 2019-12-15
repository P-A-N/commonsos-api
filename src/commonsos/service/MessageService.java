package commonsos.service;

import static java.lang.String.format;
import static java.time.Instant.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import commonsos.command.PaginationCommand;
import commonsos.command.UploadPhotoCommand;
import commonsos.command.app.CreateDirectMessageThreadCommand;
import commonsos.command.app.CreateGroupCommand;
import commonsos.command.app.CreateMessageCommand;
import commonsos.command.app.SearchMessageThreadCommand;
import commonsos.command.app.UpdateGroupMessageThreadCommand;
import commonsos.command.app.UpdateMessageThreadPersonalTitleCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;
import commonsos.service.notification.PushNotificationService;
import commonsos.service.sync.SyncService;
import commonsos.util.AdUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.UserUtil;
import commonsos.view.AdView;
import commonsos.view.MessageListView;
import commonsos.view.MessageThreadListView;
import commonsos.view.MessageThreadView;
import commonsos.view.MessageView;
import commonsos.view.UserView;

@Singleton
public class MessageService extends AbstractService {

  @Inject private MessageThreadRepository messageThreadRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private UserRepository userRepository;
  @Inject private AdRepository adRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private DeleteService deleteService;
  @Inject private SyncService syncService;
  @Inject private PushNotificationService pushNotificationService;
  @Inject private ImageUploadService imageService;

  public MessageThreadView threadForAd(User user, Long adId) {
    Ad ad = adRepository.findPublicStrictById(adId);
    User adCreator = userRepository.findStrictById(ad.getCreatedUserId());
    MessageThread thread = messageThreadRepository.findByCreaterAndAdId(user.getId(), adId).orElseGet(() -> syncService.createMessageThreadForAd(adCreator, user, adId));
    return view(user, thread);
  }

  public MessageThreadView threadWithUser(User user, CreateDirectMessageThreadCommand command) {
    User otherUser = userRepository.findStrictById(command.getOtherUserId());
    Community community = communityRepository.findPublicStrictById(command.getCommunityId());
    
    MessageThread thread = messageThreadRepository.findDirectThread(user.getId(), command.getOtherUserId(), command.getCommunityId())
      .orElseGet(() -> syncService.createMessageThreadWithUser(user, otherUser, community));
    return view(user, thread);
  }

  public MessageThreadView group(User user, CreateGroupCommand command) {
    validateCommand(command);
    
    communityRepository.findPublicStrictById(command.getCommunityId());
    if (!UserUtil.isMember(user, command.getCommunityId())) throw new BadRequestException("User isn't a member of the community");
    List<User> users = validatePartiesCommunity(command.getMemberIds(), command.getCommunityId());
    List<MessageThreadParty> parties = usersToParties(users);
    parties.add(new MessageThreadParty().setUser(user));

    MessageThread messageThread = new MessageThread()
      .setGroup(true)
      .setCommunityId(command.getCommunityId())
      .setTitle(command.getTitle())
      .setCreatedUserId(user.getId())
      .setParties(parties);
    return view(user, messageThreadRepository.create(messageThread));
  }

  private List<MessageThreadParty> usersToParties(List<User> users) {
    return users.stream().map(u -> new MessageThreadParty().setUser(u)).collect(toList());
  }

  public MessageThreadView updateGroup(User user, UpdateGroupMessageThreadCommand command) {
    validateCommand(command);
    MessageThread messageThread = messageThreadRepository.findStrictById(command.getThreadId());
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
    
    List<UserView> parties = thread.getParties().stream()
      .filter(p -> !p.getUser().getId().equals(thread.getCreatedUserId()))
      .map(MessageThreadParty::getUser)
      .map(UserUtil::publicViewForApp)
      .sorted((p1,p2) -> p1.getId().compareTo(p2.getId()))
      .collect(toList());

    UserView creator = thread.getParties().stream()
      .filter(p -> p.getUser().getId().equals(thread.getCreatedUserId()))
      .map(MessageThreadParty::getUser)
      .map(UserUtil::publicViewForApp).findFirst().orElse(null);

    UserView counterParty = concat(parties.stream(), of(creator))
      .filter(uv -> uv != null && uv.getId() != user.getId())
      .sorted((p1,p2) -> p1.getId().compareTo(p2.getId()))
      .findFirst().orElse(null);

    AdView adView = null;
    if (thread.getAdId() != null) {
      Ad ad = adRepository.findPublicStrictById(thread.getAdId());
      User createdUser = userRepository.findStrictById(ad.getCreatedUserId());
      adView = AdUtil.viewForApp(ad, createdUser, user);
    }
    MessageView lastMessage = messageRepository.findLastMessage(thread.getId()).map(this::view).orElse(null);

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
      .setCreatedBy(message.getCreatedUserId())
      .setText(message.getText());
  }

  public MessageThreadListView searchThreads(User user, SearchMessageThreadCommand command, PaginationCommand pagination) {
    communityRepository.findPublicStrictById(command.getCommunityId());
    
    List<Long> unreadMessageThreadIds = messageThreadRepository.unreadMessageThreadIds(user, command.getCommunityId());
    
    ResultList<MessageThread> result = messageThreadRepository.searchByUser(user, command.getCommunityId(), command.getMemberFilter(), command.getMessageFilter(), null);
    
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

  public MessageView postMessage(User user, CreateMessageCommand command) {
    validateCommand(command);
    
    MessageThread messageThread = messageThreadRepository.findStrictById(command.getThreadId());
    checkAccess(user, messageThread);
    Message message = messageRepository.create(new Message()
      .setCreatedUserId(user.getId())
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
        Integer unreadCount = messageRepository.unreadMessageCount(p.getUser().getId(), messageThread.getId());
        pushNotificationService.send(senderUser, p.getUser(), messageText, messageThread, unreadCount);
      });
  }

  public MessageListView messages(User user, Long threadId, PaginationCommand pagination) {
    MessageThread thread = messageThreadRepository.findStrictById(threadId);
    if (!isUserAllowedToAccessMessageThread(user, thread)) throw new ForbiddenException();

    markVisited(user, thread);

    ResultList<Message> result = messageRepository.searchByThreadId(threadId, pagination);

    MessageListView listView = new MessageListView();
    listView.setMessageList(result.getList().stream().map(this::view).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public MessageThreadView updatePersonalTitle(User user, UpdateMessageThreadPersonalTitleCommand command) {
    validateCommand(command);
    
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

  private void validateCommand(CreateGroupCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw new BadRequestException("title is empty");
    if (command.getMemberIds() == null || command.getMemberIds().isEmpty()) throw new BadRequestException("memberIds are empty");
  }

  private void validateCommand(UpdateGroupMessageThreadCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw new BadRequestException("title is empty");
    if (command.getMemberIds() == null || command.getMemberIds().isEmpty()) throw new BadRequestException("memberIds are empty");
  }

  private void validateCommand(UpdateMessageThreadPersonalTitleCommand command) {
    if (StringUtils.isEmpty(command.getPersonalTitle())) throw new BadRequestException("title is empty");
  }

  private void validateCommand(CreateMessageCommand command) {
    if (StringUtils.isEmpty(command.getText())) throw new BadRequestException("text is empty");
}
}
