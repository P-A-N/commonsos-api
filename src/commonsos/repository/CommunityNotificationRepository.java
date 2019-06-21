package commonsos.repository;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import commonsos.repository.entity.CommunityNotification;
import commonsos.repository.entity.ResultList;
import commonsos.service.command.PaginationCommand;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class CommunityNotificationRepository extends Repository {

  @Inject
  public CommunityNotificationRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<CommunityNotification> findByWordPressId(String wordpressId) {
    List<CommunityNotification> result = em().createQuery(
        "FROM CommunityNotification" +
        " WHERE wordpressId = :wordpressId", CommunityNotification.class)
        .setLockMode(lockMode())
        .setParameter("wordpressId", wordpressId)
        .getResultList();

    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 notification is found. wordpressId = %s", wordpressId));
    return Optional.of(result.get(0));
  }

  public ResultList<CommunityNotification> findByCommunityId(Long communityId, PaginationCommand pagination) {
    TypedQuery<CommunityNotification> query = em().createQuery(
        "FROM CommunityNotification" +
        " WHERE communityId = :communityId" +
        " ORDER BY id", CommunityNotification.class)
        .setLockMode(lockMode())
        .setParameter("communityId", communityId);
    
    ResultList<CommunityNotification> resultList = getResultList(query, pagination);
    
    return resultList;
  }
  
  public CommunityNotification create(CommunityNotification notification) {
    em().persist(notification);
    return notification;
  }

  public CommunityNotification update(CommunityNotification notification) {
    em().merge(notification);
    return notification;
  }
}
