package commonsos.repository;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import commonsos.command.PaginationCommand;
import commonsos.exception.AdminNotFoundException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.TemporaryAdmin;
import commonsos.repository.entity.TemporaryAdminEmailAddress;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class AdminRepository extends Repository {
  
  public static Long SEARCH_NON_COMMUNITY = -1L;

  @Inject
  public AdminRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }
  
  public Optional<Admin> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM Admin WHERE id = :id AND deleted IS FALSE", Admin.class)
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public Admin findStrictById(Long id) {
    return findById(id).orElseThrow(AdminNotFoundException::new);
  }

  public ResultList<Admin> findByCommunityId(Long communityId, PaginationCommand pagination) {
    String sql = "FROM Admin WHERE community.id = :communityId AND deleted IS FALSE ORDER BY id";
    TypedQuery<Admin> query = em().createQuery(sql, Admin.class)
        .setParameter("communityId", communityId);

    ResultList<Admin> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<Admin> findByCommunityIdAndRoleId(Long communityId, Long roleId, PaginationCommand pagination) {
    StringBuilder sql = new StringBuilder();
    sql.append("FROM Admin WHERE role.id = :roleId AND deleted IS FALSE ");
    if (SEARCH_NON_COMMUNITY.equals(communityId)) {
      sql.append("AND community.id IS null ");
    } else if (communityId != null) {
      sql.append("AND community.id = :communityId ");
    } else {
      // nothing
    }
    sql.append("ORDER BY id");

    TypedQuery<Admin> query = em().createQuery(sql.toString(), Admin.class);
    if (communityId != null && !SEARCH_NON_COMMUNITY.equals(communityId)) {
      query.setParameter("communityId", communityId);
    }
    query.setParameter("roleId", roleId);

    ResultList<Admin> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public Optional<Admin> findByEmailAddress(String emailAddress) {
    List<Admin> result = em().createQuery(
        "FROM Admin WHERE emailAddress = :emailAddress AND deleted IS FALSE", Admin.class)
        .setParameter("emailAddress", emailAddress).getResultList();

    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 admin has the same email address."));
    return Optional.of(result.get(0));
  }

  public boolean isEmailAddressTaken(String emailAddress) {
    boolean emailAddressTaken = findByEmailAddress(emailAddress).isPresent();
    
    if (!emailAddressTaken) {
      Long count = em().createQuery("SELECT COUNT(ta) FROM TemporaryAdmin ta WHERE emailAddress = :emailAddress"
          + " AND ta.invalid is FALSE"
          + " AND ta.expirationTime > CURRENT_TIMESTAMP", Long.class)
        .setParameter("emailAddress", emailAddress)
        .getSingleResult();
      
      emailAddressTaken = (count != 0);
    }
    
    if (!emailAddressTaken) {
      Long count = em().createQuery("SELECT COUNT(taea) FROM TemporaryAdminEmailAddress taea WHERE emailAddress = :emailAddress"
          + " AND taea.invalid is FALSE"
          + " AND taea.expirationTime > CURRENT_TIMESTAMP", Long.class)
        .setParameter("emailAddress", emailAddress)
        .getSingleResult();
      
      emailAddressTaken = (count != 0);
    }
    
    return emailAddressTaken;
  }
  
  public Optional<TemporaryAdmin> findTemporaryAdmin(String accessIdHash) {
    List<TemporaryAdmin> result = em().createQuery(
        "FROM TemporaryAdmin"
            + " WHERE accessIdHash = :accessIdHash"
            + " AND invalid is FALSE"
            + " AND expirationTime > CURRENT_TIMESTAMP", TemporaryAdmin.class)
        .setParameter("accessIdHash", accessIdHash)
        .getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 temporary admin has the same access id hash. accessIdHash = %s", accessIdHash));
    return Optional.of(result.get(0));
  }

  public TemporaryAdmin findStrictTemporaryAdmin(String accessIdHash) {
    return findTemporaryAdmin(accessIdHash).orElseThrow(AdminNotFoundException::new);
  }

  public Optional<TemporaryAdminEmailAddress> findTemporaryAdminEmailAddress(String accessIdHash) {
    List<TemporaryAdminEmailAddress> result = em().createQuery(
        "FROM TemporaryAdminEmailAddress"
            + " WHERE accessIdHash = :accessIdHash"
            + " AND invalid is FALSE"
            + " AND expirationTime > CURRENT_TIMESTAMP", TemporaryAdminEmailAddress.class)
        .setParameter("accessIdHash", accessIdHash)
        .getResultList();
    
    if (result.isEmpty()) return empty();
    if (result.size() > 1) log.warn(String.format("More than 1 temporary admin email address has the same access id hash. accessIdHash = %s", accessIdHash));
    return Optional.of(result.get(0));
  }

  public TemporaryAdminEmailAddress findStrictTemporaryAdminEmailAddress(String accessIdHash) {
    return findTemporaryAdminEmailAddress(accessIdHash).orElseThrow(AdminNotFoundException::new);
  }

  public Admin create(Admin admin) {
    em().persist(admin);
    return admin;
  }
  
  public TemporaryAdmin createTemporary(TemporaryAdmin tempAdmin) {
    em().persist(tempAdmin);
    return tempAdmin;
  }

  public TemporaryAdminEmailAddress createTemporaryEmail(TemporaryAdminEmailAddress tempEmailAddress) {
    em().persist(tempEmailAddress);
    return tempEmailAddress;
  }
  
  public Admin update(Admin admin) {
    checkLocked(admin);
    return em().merge(admin);
  }

  public TemporaryAdmin updateTemporaryAdmin(TemporaryAdmin tempAdmin) {
    checkLocked(tempAdmin);
    return em().merge(tempAdmin);
  }

  public TemporaryAdminEmailAddress updateTemporaryEmail(TemporaryAdminEmailAddress tempEmailAddress) {
    checkLocked(tempEmailAddress);
    return em().merge(tempEmailAddress);
  }
}
