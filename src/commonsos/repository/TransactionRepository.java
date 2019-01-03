package commonsos.repository;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Transaction;
import commonsos.repository.entity.User;

@Singleton
public class TransactionRepository extends Repository {

  @Inject
  public TransactionRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Transaction create(Transaction transaction) {
    em().persist(transaction);
    return transaction;
  }

  public List<Transaction> transactions(User user, Long communityId) {
    return em()
      .createQuery("FROM Transaction WHERE communityId = :communityId " +
          "AND (beneficiaryId = :userId OR remitterId = :userId) " + 
          "ORDER BY id", Transaction.class)
      .setLockMode(lockMode())
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId())
      .getResultList();
  }

  public void update(Transaction transaction) {
    em().merge(transaction);
  }

  public Optional<Transaction> findByBlockchainTransactionHash(String blockchainTransactionHash) {
    try {
      return of(em()
        .createQuery("From Transaction WHERE blockchainTransactionHash = :hash", Transaction.class)
        .setLockMode(lockMode())
        .setParameter("hash", blockchainTransactionHash)
        .getSingleResult());
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public boolean hasPaid(Ad ad) {
    List<Transaction> resultList = em()
      .createQuery("FROM Transaction WHERE adId = :adId", Transaction.class)
      .setLockMode(lockMode())
      .setParameter("adId", ad.getId()).getResultList();
    
    return !resultList.isEmpty();
  }

  public BigDecimal pendingTransactionsAmount(Long userId, Long communityId) {
    BigDecimal amount = em()
      .createQuery("SELECT SUM(amount) FROM Transaction " +
          "WHERE communityId = :communityId " +
          "AND blockchainCompletedAt IS NULL " +
          "AND remitterId = :userId", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", userId)
      .getSingleResult();
    return amount != null ? amount :  ZERO;
  }
}
