package commonsos.repository.user;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static spark.utils.StringUtils.isBlank;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;

import commonsos.EntityManagerService;
import commonsos.Repository;

@Singleton
public class UserRepository extends Repository {

  @Inject
  public UserRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<User> findByUsername(String username) {
    try {
      return Optional.of(em().createQuery("FROM User WHERE username = :username AND deleted = FALSE", User.class)
        .setParameter("username", username)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public User create(User user) {
    em().persist(user);
    return user;
  }

  public Optional<User> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM User WHERE id = :id AND deleted = FALSE", User.class)
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public List<User> search(Long communityId, String query) {
    if (isBlank(query)) return emptyList();
    return em().createQuery(
      "FROM User " +
      "WHERE communityId = :communityId " +
      "AND deleted = FALSE " +
      "AND LOWER(username) LIKE LOWER(:query)", User.class)
      .setParameter("communityId", communityId)
      .setParameter("query", "%"+query+"%")
      .setMaxResults(10)
      .getResultList();
  }

  public User update(User user) {
    return em().merge(user);
  }

  public User findAdminByCommunityId(Long communityId) {
    return em().createQuery("FROM User WHERE admin = TRUE and communityId = :communityId AND deleted = FALSE", User.class)
      .setParameter("communityId", communityId)
      .getSingleResult();
  }
}
