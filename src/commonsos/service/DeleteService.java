package commonsos.service;

import static java.time.Instant.now;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;
import commonsos.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DeleteService {

  @Inject private UserRepository userRepository;
  @Inject private AdRepository adRepository;
  @Inject private MessageThreadRepository messageThreadRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private ImageUploadService imageService;

  public void deleteUser(User user) {
    log.info(String.format("deleting user. userId=%d", user.getId()));
    
    // delete user's ads
    List<Ad> myAds = adRepository.myAds(user.getId());
    myAds.forEach(ad -> deleteAd(user, ad));
    
    // delete user's message threads party
    List<MessageThread> myThreads = messageThreadRepository.listByUser(user);
    myThreads.forEach(thread -> deleteMessageThreadParty(user, thread.getId()));
    
    // delete user's photo
    deletePhoto(user.getAvatarUrl());
    
    // delete user(logically)
    user.setDeleted(true);
    userRepository.update(user);

    log.info(String.format("deleted user. userId=%d", user.getId()));
  }
  
  public void deleteAd(User user, Ad ad) {
    log.info(String.format("deleting ad. adId=%d, userId=%d", ad.getId(), user.getId()));
    
    // only creator is allowed to delete ad
    if (!ad.getCreatedBy().equals(user.getId())) throw new ForbiddenException();

    // delete ad's photo
    deletePhoto(ad.getPhotoUrl());
    
    // delete ad(logically)
    ad.setDeleted(true);
    adRepository.update(ad);
    
    log.info(String.format("deleted ad. adId=%d, userId=%d", ad.getId(), user.getId()));
  }

  public void deleteMessageThreadParty(User user, Long threadId) {
    log.info(String.format("deleting message-thread-party. threadId=%d, userId=%d", threadId, user.getId()));
    
    // verify user is a member of thread
    MessageThread thread = messageThreadRepository.findStrictById(threadId);
    Optional<MessageThreadParty> userMtp = thread.getParties().stream().filter(p -> p.getUser().getId().equals(user.getId())).findFirst();
    if (!userMtp.isPresent()) throw new BadRequestException("User is not a member of thread");
    
    // delete message-thread-party's photo
    deletePhoto(userMtp.get().getPhotoUrl());
    
    // delete message-thread-party
    messageThreadRepository.deleteMessageThreadParty(user, threadId);
    
    // send system message to message-thread
    Message systemMessage = new Message()
        .setCreatedBy(MessageUtil.getSystemMessageCreatorId())
        .setCreatedAt(now())
        .setThreadId(thread.getId())
        .setText(MessageUtil.getSystemMessageUnsubscribe(user.getUsername()));
    messageRepository.create(systemMessage);

    log.info(String.format("deleted message-thread-party. threadId=%d, userId=%d", threadId, user.getId()));
  }
  
  public void deletePhoto(String photoUrl) {
    log.info(String.format("deleting photo. url=%s", photoUrl));
    imageService.delete(photoUrl);
    log.info(String.format("deleted photo. url=%s", photoUrl));
  }
}
