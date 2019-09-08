package commonsos.repository;

import static java.util.Optional.empty;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import commonsos.controller.command.PaginationCommand;
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

  public ResultList<Ad> ads(Long communityId, PaginationCommand pagination) {
    TypedQuery<Ad> query = em()
      .createQuery("FROM Ad WHERE communityId = :communityId AND deleted = FALSE ORDER BY id", Ad.class)
      .setLockMode(lockMode())
      .setParameter("communityId", communityId);
    
    ResultList<Ad> resultList = getResultList(query, pagination);

    return resultList;
  }

  public ResultList<Ad> ads(Long communityId, String filter, PaginationCommand pagination) {
    TypedQuery<Ad> query = em()
      .createQuery("SELECT a FROM Ad a JOIN User u ON a.createdBy = u.id AND u.deleted = FALSE" +
        " WHERE a.communityId = :communityId " +
        " AND a.deleted = FALSE" +
        " AND (" +
          " LOWER(a.description) LIKE LOWER(:filter) OR LOWER(a.title) LIKE LOWER(:filter) " +
          " OR LOWER(u.username) LIKE LOWER(:filter) " +
        " )" +
        " ORDER BY a.id", Ad.class)
      .setLockMode(lockMode())
      .setParameter("communityId", communityId)
      .setParameter("filter", "%"+filter+"%");
    
    ResultList<Ad> resultList = getResultList(query, pagination);

    return resultList;
  }

  public ResultList<Ad> myAds(Long userId, PaginationCommand pagination) {
    TypedQuery<Ad> query = em()
      .createQuery("FROM Ad WHERE createdBy = :userId AND deleted = FALSE ORDER BY id", Ad.class)
      .setLockMode(lockMode())
      .setParameter("userId", userId);
    
    ResultList<Ad> resultList = getResultList(query, pagination);

    return resultList;
  }

  public Optional<Ad> find(Long id) {
    try {
      return Optional.of(em()
        .createQuery("FROM Ad WHERE id = :id AND deleted = FALSE", Ad.class)
        .setLockMode(lockMode())
        .setParameter("id", id)
        .getSingleResult());
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public Ad findStrict(Long id) {
    return find(id).orElseThrow(AdNotFoundException::new);
  }

  public Ad update(Ad ad) {
    em().merge(ad);
    return ad;
  }
}
