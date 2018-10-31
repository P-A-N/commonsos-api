package commonsos.repository;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static spark.utils.StringUtils.isBlank;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;

import commonsos.exception.UserNotFoundException;
import commonsos.repository.entity.PasswordResetRequest;
import commonsos.repository.entity.TemporaryEmailAddress;
import commonsos.repository.entity.TemporaryUser;
import commonsos.repository.entity.User;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class UserRepository extends Repository {

  @Inject
  public UserRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<User> findByUsername(String username) {
    List<User> result = em().createQuery(
        "FROM User WHERE username = :username AND deleted = FALSE", User.class)
        .setParameter("username", username).getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 user has the same name. username = %s", username));
    return Optional.of(result.get(0));
  }

  public Optional<User> findByEmailAddress(String emailAddress) {
    List<User> result = em().createQuery(
        "FROM User WHERE emailAddress = :emailAddress AND deleted = FALSE", User.class)
        .setParameter("emailAddress", emailAddress).getResultList();

    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 user has the same email address."));
    return Optional.of(result.get(0));
  }

  public boolean isUsernameTaken(String username) {
    boolean usernameTaken = findByUsername(username).isPresent();
    
    if (!usernameTaken) {
      Long count = em().createQuery("SELECT COUNT(tu) FROM TemporaryUser tu WHERE username = :username"
          + " AND tu.invalid is FALSE"
          + " AND tu.expirationTime > CURRENT_TIMESTAMP", Long.class)
        .setParameter("username", username)
        .getSingleResult();
      
      usernameTaken = (count != 0);
    }
    
    return usernameTaken;
  }

  public boolean isEmailAddressTaken(String emailAddress) {
    boolean usernameTaken = findByEmailAddress(emailAddress).isPresent();
    
    if (!usernameTaken) {
      Long count = em().createQuery("SELECT COUNT(tu) FROM TemporaryUser tu WHERE emailAddress = :emailAddress"
          + " AND tu.invalid is FALSE"
          + " AND tu.expirationTime > CURRENT_TIMESTAMP", Long.class)
        .setParameter("emailAddress", emailAddress)
        .getSingleResult();
      
      usernameTaken = (count != 0);
    }
    
    if (!usernameTaken) {
      Long count = em().createQuery("SELECT COUNT(tea) FROM TemporaryEmailAddress tea WHERE emailAddress = :emailAddress"
          + " AND tea.invalid is FALSE"
          + " AND tea.expirationTime > CURRENT_TIMESTAMP", Long.class)
        .setParameter("emailAddress", emailAddress)
        .getSingleResult();
      
      usernameTaken = (count != 0);
    }
    
    return usernameTaken;
  }
  
  public User create(User user) {
    em().persist(user);
    return user;
  }
  
  public TemporaryUser createTemporary(TemporaryUser temporaryUser) {
    em().persist(temporaryUser);
    return temporaryUser;
  }
  
  public TemporaryEmailAddress createTemporaryEmailAddress(TemporaryEmailAddress temporaryEmailAddress) {
    em().persist(temporaryEmailAddress);
    return temporaryEmailAddress;
  }
  
  public PasswordResetRequest createPasswordResetRequest(PasswordResetRequest passwordResetRequest) {
    em().persist(passwordResetRequest);
    return passwordResetRequest;
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

  public User findStrictById(Long id) {
    return findById(id).orElseThrow(UserNotFoundException::new);
  }

  public Optional<TemporaryUser> findTemporaryUser(String accessIdHash) {
    List<TemporaryUser> result = em().createQuery(
        "FROM TemporaryUser"
            + " WHERE accessIdHash = :accessIdHash"
            + " AND invalid is FALSE"
            + " AND expirationTime > CURRENT_TIMESTAMP", TemporaryUser.class)
        .setParameter("accessIdHash", accessIdHash).getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 temporary user has the same access id hash. accessIdHash = %s", accessIdHash));
    return Optional.of(result.get(0));
  }

  public TemporaryUser findStrictTemporaryUser(String accessIdHash) {
    return findTemporaryUser(accessIdHash).orElseThrow(UserNotFoundException::new);
  }

  public Optional<TemporaryEmailAddress> findTemporaryEmailAddress(String accessIdHash) {
    List<TemporaryEmailAddress> result = em().createQuery(
        "FROM TemporaryEmailAddress"
            + " WHERE accessIdHash = :accessIdHash"
            + " AND invalid is FALSE"
            + " AND expirationTime > CURRENT_TIMESTAMP", TemporaryEmailAddress.class)
        .setParameter("accessIdHash", accessIdHash).getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 temporary email address has the same access id hash. accessIdHash = %s", accessIdHash));
    return Optional.of(result.get(0));
  }

  public TemporaryEmailAddress findStrictTemporaryEmailAddress(String accessIdHash) {
    return findTemporaryEmailAddress(accessIdHash).orElseThrow(UserNotFoundException::new);
  }

  public Optional<PasswordResetRequest> findPasswordResetRequest(String accessIdHash) {
    List<PasswordResetRequest> result = em().createQuery(
        "FROM PasswordResetRequest"
            + " WHERE accessIdHash = :accessIdHash"
            + " AND invalid is FALSE"
            + " AND expirationTime > CURRENT_TIMESTAMP", PasswordResetRequest.class)
        .setParameter("accessIdHash", accessIdHash).getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 passward reset request has the same access id hash. accessIdHash = %s", accessIdHash));
    return Optional.of(result.get(0));
  }

  public PasswordResetRequest findStrictPasswordResetRequest(String accessIdHash) {
    return findPasswordResetRequest(accessIdHash).orElseThrow(UserNotFoundException::new);
  }

  public List<User> search(Long communityId, String query) {
    if (isBlank(query)) return emptyList();
    return em().createQuery(
      "SELECT u FROM User u JOIN u.communityList c " +
      "WHERE c.id = :communityId " +
      "AND u.deleted = FALSE " +
      "AND LOWER(u.username) LIKE LOWER(:query)", User.class)
      .setParameter("communityId", communityId)
      .setParameter("query", "%"+query+"%")
      .setMaxResults(10)
      .getResultList();
  }

  public User update(User user) {
    return em().merge(user);
  }

  public TemporaryUser updateTemporary(TemporaryUser temporaryUser) {
    return em().merge(temporaryUser);
  }

  public TemporaryEmailAddress updateTemporaryEmailAddress(TemporaryEmailAddress temporaryEmailAddress) {
    return em().merge(temporaryEmailAddress);
  }

  public PasswordResetRequest updatePasswordResetRequest(PasswordResetRequest passwordResetRequest) {
    return em().merge(passwordResetRequest);
  }
}
