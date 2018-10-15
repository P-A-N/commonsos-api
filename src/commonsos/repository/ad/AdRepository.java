package commonsos.repository.ad;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;

import commonsos.EntityManagerService;
import commonsos.Repository;

@Singleton
public class AdRepository extends Repository {

  @Inject public AdRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Ad create(Ad ad) {
    em().persist(ad);
    return ad;
  }

  public List<Ad> ads(Long communityId) {
    return em()
      .createQuery("FROM Ad WHERE communityId = :communityId AND deleted = FALSE", Ad.class)
      .setParameter("communityId", communityId).getResultList();
  }

  public List<Ad> ads(Long communityId, String filter) {
    return em()
      .createQuery("SELECT a FROM Ad a JOIN User u ON a.createdBy = u.id AND u.deleted = FALSE" +
        " WHERE a.communityId = :communityId " +
        " AND a.deleted = FALSE" +
        " AND (" +
          " LOWER(a.description) LIKE LOWER(:filter) OR LOWER(a.title) LIKE LOWER(:filter) " +
          " OR LOWER(u.username) LIKE LOWER(:filter) " +
        " )", Ad.class)
      .setParameter("communityId", communityId)
      .setParameter("filter", "%"+filter+"%")
      .getResultList();
  }

  public List<Ad> myAds(List<Long> communityIdList, Long userId) {
    return em()
      .createQuery("FROM Ad WHERE communityId in :communityIdList AND createdBy = :userId AND deleted = FALSE", Ad.class)
      .setParameter("communityIdList", communityIdList)
      .setParameter("userId", userId)
      .getResultList();
  }

  public Optional<Ad> find(Long id) {
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

  public Ad update(Ad ad) {
    em().merge(ad);
    return ad;
  }
}
