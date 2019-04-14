package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityNotificationRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityNotification;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.command.CommunityNotificationCommand;
import commonsos.service.command.PaginationCommand;
import commonsos.service.command.UploadPhotoCommand;
import commonsos.service.image.ImageUploadService;
import commonsos.util.CommunityUtil;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityListView;
import commonsos.view.CommunityNotificationListView;

@Singleton
public class CommunityService {

  @Inject private CommunityRepository repository;
  @Inject private CommunityNotificationRepository notificationRepository;
  @Inject private ImageUploadService imageService;
  @Inject private BlockchainService blockchainService;

  public CommunityListView list(String filter, PaginationCommand pagination) {
    ResultList<Community> result = StringUtils.isEmpty(filter) ? repository.list(pagination) : repository.list(filter, pagination);

    CommunityListView listView = new CommunityListView();
    listView.setCommunityList(result.getList().stream().map(c -> CommunityUtil.view(c, blockchainService.tokenSymbol(c.getId()))).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public Community community(Long id) {
    return repository.findById(id).orElseThrow(() -> new BadRequestException("community not found"));
  }

  public boolean isAdmin(Long userId, Long communityId) {
    return repository.isAdmin(userId, communityId);
  }

  public String updatePhoto(User user, UploadPhotoCommand command, Long communityId) {
    Community community = repository.findStrictById(communityId);
    if (!repository.isAdmin(user.getId(), communityId)) throw new ForbiddenException("User is not admin");
    
    String url = imageService.create(command);
    imageService.delete(community.getPhotoUrl());
    
    community.setPhotoUrl(url);
    repository.update(community);
    return url;
  }

  public String updateCoverPhoto(User user, UploadPhotoCommand command, Long communityId) {
    Community community = repository.findStrictById(communityId);
    if (!repository.isAdmin(user.getId(), communityId)) throw new ForbiddenException("User is not admin");
    
    String url = imageService.create(command);
    imageService.delete(community.getCoverPhotoUrl());
    
    community.setCoverPhotoUrl(url);
    repository.update(community);
    return url;
  }
  
  public void updateNotificationUpdateAt(CommunityNotificationCommand command) {
    Optional<CommunityNotification> optionalNotification = notificationRepository.findByWordPressId(command.getWordpressId());
    if (optionalNotification.isPresent()) {
      CommunityNotification notification = optionalNotification.get();
      
      if (!notification.getCommunityId().equals(command.getCommunityId())) throw new BadRequestException(
          String.format("it is not a notification of community. wordpressId=%s communityId=%d", command.getWordpressId(), command.getCommunityId()));
      
      notification.setUpdatedAt(command.getUpdatedAtInstant());
      notificationRepository.update(notification);
    } else {
      CommunityNotification notification = new CommunityNotification()
          .setCommunityId(command.getCommunityId())
          .setWordpressId(command.getWordpressId())
          .setUpdatedAt(command.getUpdatedAtInstant());

      notificationRepository.create(notification);
    }
  }
  
  public CommunityNotificationListView notificationList(Long communityId, PaginationCommand pagination) {
    repository.findStrictById(communityId);
    
    ResultList<CommunityNotification> result = notificationRepository.findByCommunityId(communityId, pagination);

    CommunityNotificationListView listView = new CommunityNotificationListView();
    listView.setNotificationList(CommunityUtil.notificationView(result.getList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }
}
