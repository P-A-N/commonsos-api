package commonsos.repository;

import static java.util.Optional.empty;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import commonsos.command.PaginationCommand;
import commonsos.exception.AdNotFoundException;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;

@Singleton
public class AdRepository extends Repository {

  @Inject public AdRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Ad create(Ad ad) {
    em().persist(ad);
    return ad;
  }

  public ResultList<Ad> searchByCommunityId(Long communityId, PaginationCommand pagination) {
    TypedQuery<Ad> query = em()
      .createQuery("FROM Ad WHERE communityId = :communityId AND deleted = FALSE ORDER BY id", Ad.class)
      .setParameter("communityId", communityId);
    
    ResultList<Ad> resultList = getResultList(query, pagination);

    return resultList;
  }

  public ResultList<Ad> searchPublicByCommunityId(Long communityId, PaginationCommand pagination) {
    TypedQuery<Ad> query = em()
      .createQuery("FROM Ad WHERE communityId = :communityId AND deleted = FALSE AND publishStatus = 'PUBLIC' ORDER BY id", Ad.class)
      .setParameter("communityId", communityId);
    
    ResultList<Ad> resultList = getResultList(query, pagination);

    return resultList;
  }

  public ResultList<Ad> searchPublicByCommunityId(Long communityId, String filter, PaginationCommand pagination) {
    TypedQuery<Ad> query = em()
      .createQuery("SELECT a FROM Ad a JOIN User u ON a.createdUserId = u.id AND u.deleted = FALSE" +
        " WHERE a.communityId = :communityId " +
        " AND a.deleted = FALSE" +
        " AND a.publishStatus = 'PUBLIC'" +
        " AND (" +
          " LOWER(a.description) LIKE LOWER(:filter) OR LOWER(a.title) LIKE LOWER(:filter) " +
          " OR LOWER(u.username) LIKE LOWER(:filter) " +
        " )" +
        " ORDER BY a.id", Ad.class)
      .setParameter("communityId", communityId)
      .setParameter("filter", "%"+filter+"%");
    
    ResultList<Ad> resultList = getResultList(query, pagination);

    return resultList;
  }

  public ResultList<Ad> searchByCreatorId(Long userId, PaginationCommand pagination) {
    TypedQuery<Ad> query = em()
      .createQuery("FROM Ad WHERE createdUserId = :userId AND deleted = FALSE ORDER BY id", Ad.class)
      .setParameter("userId", userId);
    
    ResultList<Ad> resultList = getResultList(query, pagination);

    return resultList;
  }

  public Optional<Ad> findPublicById(Long id) {
    try {
      return Optional.of(em()
        .createQuery("FROM Ad WHERE id = :id AND deleted = FALSE AND publishStatus = 'PUBLIC'", Ad.class)
        .setParameter("id", id)
        .getSingleResult());
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public Ad findPublicStrictById(Long id) {
    return findPublicById(id).orElseThrow(AdNotFoundException::new);
  }

  public Optional<Ad> findById(Long id) {
    try {
      return Optional.of(em()
        .createQuery("FROM Ad WHERE id = :id AND deleted = FALSE", Ad.class)
        .setParameter("id", id)
        .getSingleResult());
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public Ad findStrictById(Long id) {
    return findById(id).orElseThrow(AdNotFoundException::new);
  }

  public Ad update(Ad ad) {
    checkLocked(ad);
    return em().merge(ad);
  }
}
