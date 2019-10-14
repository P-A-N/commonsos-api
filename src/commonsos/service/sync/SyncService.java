package commonsos.service.sync;

import static java.util.Arrays.asList;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.annotation.SyncObject;
import commonsos.annotation.Synchronized;
import commonsos.exception.BadRequestException;
import commonsos.repository.AdRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;
import commonsos.service.AbstractService;
import commonsos.util.MessageUtil;
import commonsos.util.UserUtil;

@Singleton
public class SyncService extends AbstractService {

  @Inject private MessageThreadRepository messageThreadRepository;
  @Inject private UserRepository userRepository;
  @Inject private AdRepository adRepository;

  @Synchronized(SyncObject.MESSAGE_THRED_FOR_AD)
  public MessageThread createMessageThreadForAd(User user, Long adId) {
    Optional<MessageThread> optional = messageThreadRepository.byCreaterAndAdId(user.getId(), adId);
    if (optional.isPresent()) return optional.get();
    
    Ad ad = adRepository.findStrict(adId);
    User adCreator = userRepository.findStrictById(ad.getCreatedUserId());

    MessageThread messageThread = new MessageThread()
      .setCommunityId(ad.getCommunityId())
      .setCreatedUserId(user.getId())
      .setTitle(ad.getTitle()).setAdId(adId)
      .setParties(asList(new MessageThreadParty().setUser(adCreator), new MessageThreadParty().setUser(user)));

    return messageThreadRepository.create(messageThread);
  }

  @Synchronized(SyncObject.MESSAGE_THRED_BETWEEN_USER)
  public MessageThread createMessageThreadWithUser(User user, User otherUser, Community community) {
    if (!UserUtil.isMember(user, community.getId()) || !UserUtil.isMember(otherUser, community.getId())) throw new BadRequestException(String.format("User isn't a member of the community. [communityId=%d]", community.getId()));
  
    Optional<MessageThread> optional = messageThreadRepository.betweenUsers(user.getId(), otherUser.getId(), community.getId());
    if (optional.isPresent()) return optional.get();
    
    MessageThread messageThread = new MessageThread()
      .setCommunityId(community.getId())
      .setCreatedUserId(user.getId())
      .setParties(asList(new MessageThreadParty().setUser(user), new MessageThreadParty().setUser(otherUser)));

    return messageThreadRepository.create(messageThread);
  }

  @Synchronized(SyncObject.MESSAGE_THRED_BETWEEN_USER)
  public MessageThread createMessageThreadWithSystem(User user, Community community) {
    if (!UserUtil.isMember(user, community.getId())) throw new BadRequestException(String.format("User isn't a member of the community. [communityId=%d]", community.getId()));
  
    Optional<MessageThread> optional = messageThreadRepository.betweenUsers(user.getId(), MessageUtil.getSystemMessageCreatorId(), community.getId());
    if (optional.isPresent()) return optional.get();
    
    MessageThread messageThread = new MessageThread()
      .setCommunityId(community.getId())
      .setCreatedUserId(user.getId())
      .setParties(asList(new MessageThreadParty().setUser(user), new MessageThreadParty().setUser(new User().setId(MessageUtil.getSystemMessageCreatorId()))));

    return messageThreadRepository.create(messageThread);
  }
}
