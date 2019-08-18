package commonsos.repository;

import static java.util.Optional.empty;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import commonsos.exception.RedistributionNotFoundException;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.ResultList;
import commonsos.service.command.PaginationCommand;

@Singleton
public class RedistributionRepository extends Repository {

  @Inject
  public RedistributionRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<Redistribution> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM Redistribution WHERE id = :id AND deleted IS FALSE", Redistribution.class)
        .setLockMode(lockMode())
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public Redistribution findStrictById(Long id) {
    return findById(id).orElseThrow(RedistributionNotFoundException::new);
  }

  public ResultList<Redistribution> findByCommunityId(Long communityId, PaginationCommand pagination) {
    String sql = "FROM Redistribution WHERE community.id = :communityId AND deleted IS FALSE ORDER BY id";
    TypedQuery<Redistribution> query = em().createQuery(sql, Redistribution.class)
        .setLockMode(lockMode())
        .setParameter("communityId", communityId);

    ResultList<Redistribution> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public Redistribution create(Redistribution redistribution) {
    em().persist(redistribution);
    return redistribution;
  }

  public Redistribution update(Redistribution redistribution) {
    return em().merge(redistribution);
  }
}
