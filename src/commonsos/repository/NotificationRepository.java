package commonsos.repository;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;

import commonsos.exception.NotificationNotFoundException;
import commonsos.repository.entity.Notification;

@Singleton
public class NotificationRepository extends Repository {

  @Inject
  public NotificationRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }
  public Optional<Notification> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM Notification WHERE id = :id AND deleted IS FALSE", Notification.class)
        .setLockMode(lockMode())
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public Notification findStrictById(Long id) {
    return findById(id).orElseThrow(NotificationNotFoundException::new);
  }

  public List<Notification> search(Long communityId) {
    return em().createQuery(
        "FROM Notification" +
        " WHERE communityId = :communityId" +
        " AND deleted IS FALSE" +
        " ORDER BY createdAt desc, id", Notification.class)
        .setLockMode(lockMode())
        .setParameter("communityId", communityId)
        .getResultList();
  }

  public Notification create(Notification notification) {
    em().persist(notification);
    return notification;
  }

  public Notification update(Notification notification) {
    return em().merge(notification);
  }
}
