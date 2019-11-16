package commonsos.repository;

import static java.util.Optional.empty;

import java.time.LocalDate;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import commonsos.command.PaginationCommand;
import commonsos.exception.EthBalanceHistoryNotFoundException;
import commonsos.repository.entity.EthBalanceHistory;
import commonsos.repository.entity.ResultList;

@Singleton
public class EthBalanceHistoryRepository extends Repository {

  private static LocalDate MIN = LocalDate.of(1970, 1, 1);
  private static LocalDate MAX = LocalDate.of(9999, 12, 31);

  @Inject
  public EthBalanceHistoryRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<EthBalanceHistory> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM EthBalanceHistory WHERE id = :id", EthBalanceHistory.class)
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public EthBalanceHistory findStrictById(Long id) {
    return findById(id).orElseThrow(EthBalanceHistoryNotFoundException::new);
  }

  public Optional<EthBalanceHistory> findByCommunityIdAndBaseDate(Long communityId, LocalDate baseDate) {
    try {
      return Optional.of(em().createQuery("FROM EthBalanceHistory WHERE community.id = :communityId AND baseDate = :baseDate", EthBalanceHistory.class)
        .setParameter("communityId", communityId)
        .setParameter("baseDate", baseDate)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public ResultList<EthBalanceHistory> searchByCommunityId(Long communityId, PaginationCommand pagination) {
    String sql = "FROM EthBalanceHistory WHERE community.id = :communityId ORDER BY baseDate";
    TypedQuery<EthBalanceHistory> query = em().createQuery(sql, EthBalanceHistory.class)
        .setParameter("communityId", communityId);

    ResultList<EthBalanceHistory> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<EthBalanceHistory> searchByCommunityIdAndBaseDate(Long communityId, LocalDate from, LocalDate to, PaginationCommand pagination) {
    String sql = "FROM EthBalanceHistory WHERE community.id = :communityId AND baseDate BETWEEN :from AND :to ORDER BY baseDate";
    TypedQuery<EthBalanceHistory> query = em().createQuery(sql, EthBalanceHistory.class);
    query.setParameter("communityId", communityId);
    query.setParameter("from", from == null ? MIN : from);
    query.setParameter("to", to == null ? MAX : to);

    ResultList<EthBalanceHistory> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public EthBalanceHistory create(EthBalanceHistory ethBalanceHistory) {
    em().persist(ethBalanceHistory);
    return ethBalanceHistory;
  }

  public EthBalanceHistory update(EthBalanceHistory ethBalanceHistory) {
    checkLocked(ethBalanceHistory);
    return em().merge(ethBalanceHistory);
  }
}
