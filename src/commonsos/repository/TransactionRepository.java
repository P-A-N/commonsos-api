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
import javax.persistence.TypedQuery;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.Transaction;
import commonsos.repository.entity.User;
import commonsos.service.command.PaginationCommand;

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

  public ResultList<Transaction> transactions(User user, Long communityId, PaginationCommand pagination) {
    TypedQuery<Transaction> query = em()
      .createQuery("FROM Transaction WHERE communityId = :communityId " +
          "AND (beneficiaryId = :userId OR remitterId = :userId) " + 
          "ORDER BY id", Transaction.class)
      .setLockMode(lockMode())
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId());
    
    ResultList<Transaction> resultList = getResultList(query, pagination);
    
    return resultList;
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
      .setParameter("adId", ad.getId())
      .getResultList();
    
    return !resultList.isEmpty();
  }

  public BigDecimal getBalanceFromTransactions(User user, Long communityId) {
    BigDecimal remitAmount = em()
      .createQuery("SELECT SUM(amount) FROM Transaction WHERE communityId = :communityId AND remitterId = :userId ", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId())
      .getSingleResult();
    if (remitAmount == null) remitAmount = BigDecimal.ZERO;
    
    BigDecimal benefitAmount = em()
      .createQuery("SELECT SUM(amount) FROM Transaction WHERE communityId = :communityId AND beneficiaryId = :userId ", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId())
      .getSingleResult();
    if (benefitAmount == null) benefitAmount = BigDecimal.ZERO;
    
    return benefitAmount.subtract(remitAmount);
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
  
  public Long pendingTransactionsCount() {
    Long count = em()
      .createQuery("SELECT COUNT(*) FROM Transaction " +
          "WHERE blockchainCompletedAt IS NULL ", Long.class)
      .getSingleResult();
    return count != null ? count :  0L;
  }
}
