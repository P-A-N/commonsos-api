package commonsos.service;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.NotificationRepository;
import commonsos.repository.entity.Notification;
import commonsos.repository.entity.User;
import commonsos.service.command.CreateNotificationCommand;
import commonsos.service.command.UpdateNotificationCommand;
import commonsos.util.NotificationUtil;
import commonsos.util.ValidateUtil;
import commonsos.view.NotificationView;

@Singleton
public class NotificationService {

  @Inject NotificationRepository repository;
  @Inject CommunityRepository communityRepository;

  public Notification create(User user, CreateNotificationCommand command) {
    if (!communityRepository.isAdmin(user.getId(), command.getCommunityId())) throw new ForbiddenException("Only admin is allowed");
    ValidateUtil.validateUrl(command.getUrl());
    
    Notification notification = new Notification()
        .setCommunityId(command.getCommunityId())
        .setTitle(command.getTitle())
        .setUrl(command.getUrl())
        .setCreatedBy(user.getId())
        .setCreatedAt(now());

    return repository.create(notification);
  }
  
  public Notification update(User user, UpdateNotificationCommand command) {
    Notification notification = repository.findStrictById(command.getId());
    
    if (!communityRepository.isAdmin(user.getId(), notification.getCommunityId())) throw new ForbiddenException("Only admin is allowed");
    ValidateUtil.validateUrl(command.getUrl());
    
    notification
        .setTitle(command.getTitle())
        .setUrl(command.getUrl());

    return repository.update(notification);
  }
  
  public void deleteLogically(User user, Long id) {
    Notification notification = repository.findStrictById(id);
    
    if (!communityRepository.isAdmin(user.getId(), notification.getCommunityId())) throw new ForbiddenException("Only admin is allowed");
    
    notification.setDeleted(true);
    repository.update(notification);
  }
  
  public List<NotificationView> search(Long communityId) {
    return repository.search(communityId).stream().map(NotificationUtil::view).collect(toList());
  }
}
