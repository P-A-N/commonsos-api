package commonsos.repository;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import commonsos.exception.UserNotFoundException;
import commonsos.repository.entity.PasswordResetRequest;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.TemporaryEmailAddress;
import commonsos.repository.entity.TemporaryUser;
import commonsos.repository.entity.User;
import commonsos.service.command.PaginationCommand;
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
        .setLockMode(lockMode())
        .setParameter("username", username).getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 user has the same name. username = %s", username));
    return Optional.of(result.get(0));
  }

  public Optional<User> findByEmailAddress(String emailAddress) {
    List<User> result = em().createQuery(
        "FROM User WHERE emailAddress = :emailAddress AND deleted = FALSE", User.class)
        .setLockMode(lockMode())
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
    boolean emailAddressTaken = findByEmailAddress(emailAddress).isPresent();
    
    if (!emailAddressTaken) {
      Long count = em().createQuery("SELECT COUNT(tu) FROM TemporaryUser tu WHERE emailAddress = :emailAddress"
          + " AND tu.invalid is FALSE"
          + " AND tu.expirationTime > CURRENT_TIMESTAMP", Long.class)
        .setParameter("emailAddress", emailAddress)
        .getSingleResult();
      
      emailAddressTaken = (count != 0);
    }
    
    if (!emailAddressTaken) {
      Long count = em().createQuery("SELECT COUNT(tea) FROM TemporaryEmailAddress tea WHERE emailAddress = :emailAddress"
          + " AND tea.invalid is FALSE"
          + " AND tea.expirationTime > CURRENT_TIMESTAMP", Long.class)
        .setParameter("emailAddress", emailAddress)
        .getSingleResult();
      
      emailAddressTaken = (count != 0);
    }
    
    return emailAddressTaken;
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
      return Optional.of(em().createQuery("FROM User WHERE id = :id AND deleted IS FALSE", User.class)
        .setLockMode(lockMode())
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
        .setLockMode(lockMode())
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
        .setLockMode(lockMode())
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
        .setLockMode(lockMode())
        .setParameter("accessIdHash", accessIdHash).getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 passward reset request has the same access id hash. accessIdHash = %s", accessIdHash));
    return Optional.of(result.get(0));
  }

  public PasswordResetRequest findStrictPasswordResetRequest(String accessIdHash) {
    return findPasswordResetRequest(accessIdHash).orElseThrow(UserNotFoundException::new);
  }

  public ResultList<User> search(Long communityId, String q, PaginationCommand pagination) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT u FROM User u ");
    sql.append("WHERE EXISTS (FROM CommunityUser cu WHERE cu.userId = u.id AND cu.community.id = :communityId) ");
    sql.append("AND u.deleted IS FALSE ");
    if (StringUtils.isNotEmpty(q)) {
      sql.append("AND LOWER(u.username) LIKE LOWER(:q) ");
    }
    sql.append("ORDER BY u.id");
    
    TypedQuery<User> query = em().createQuery(sql.toString(), User.class)
      .setLockMode(lockMode())
      .setParameter("communityId", communityId);
    if (StringUtils.isNotEmpty(q)) {
      query.setParameter("q", "%"+q+"%");
    }
    
    ResultList<User> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<User> search(
      String username,
      String emailAddress,
      Long communityId,
      PaginationCommand pagination) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT u FROM User u ");
    sql.append("WHERE u.deleted IS FALSE  ");
    if (StringUtils.isNotEmpty(username)) {
      sql.append("AND LOWER(u.username) LIKE LOWER(:username) ");
    }
    if (StringUtils.isNotEmpty(emailAddress)) {
      sql.append("AND LOWER(u.emailAddress) LIKE LOWER(:emailAddress) ");
    }
    if (communityId != null) {
      sql.append("AND EXISTS (FROM CommunityUser cu WHERE cu.userId = u.id AND cu.community.id = :communityId) ");
    }
    sql.append("ORDER BY u.id");
    
    TypedQuery<User> query = em().createQuery(sql.toString(), User.class)
      .setLockMode(lockMode());
    if (StringUtils.isNotEmpty(username)) {
      query.setParameter("username", "%"+username+"%");
    }
    if (StringUtils.isNotEmpty(emailAddress)) {
      query.setParameter("emailAddress", "%"+emailAddress+"%");
    }
    if (communityId != null) {
      query.setParameter("communityId", communityId);
    }
    
    ResultList<User> resultList = getResultList(query, pagination);
    
    return resultList;
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
