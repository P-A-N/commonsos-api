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

import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;

@Singleton
public class TokenTransactionRepository extends Repository {

  @Inject
  public TokenTransactionRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public TokenTransaction create(TokenTransaction transaction) {
    em().persist(transaction);
    return transaction;
  }

  public ResultList<TokenTransaction> transactions(User user, Long communityId, PaginationCommand pagination) {
    TypedQuery<TokenTransaction> query = em()
      .createQuery("FROM TokenTransaction WHERE communityId = :communityId " +
          "AND (beneficiaryId = :userId OR remitterId = :userId) " + 
          "ORDER BY id", TokenTransaction.class)
      .setLockMode(lockMode())
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId());
    
    ResultList<TokenTransaction> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public TokenTransaction update(TokenTransaction transaction) {
    return em().merge(transaction);
  }

  public Optional<TokenTransaction> findByBlockchainTransactionHash(String blockchainTransactionHash) {
    try {
      return of(em()
        .createQuery("From TokenTransaction WHERE blockchainTransactionHash = :hash", TokenTransaction.class)
        .setLockMode(lockMode())
        .setParameter("hash", blockchainTransactionHash)
        .getSingleResult());
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public boolean hasPaid(Ad ad) {
    List<TokenTransaction> resultList = em()
      .createQuery("FROM TokenTransaction WHERE adId = :adId", TokenTransaction.class)
      .setLockMode(lockMode())
      .setParameter("adId", ad.getId())
      .getResultList();
    
    return !resultList.isEmpty();
  }

  public BigDecimal getBalanceFromTransactions(User user, Long communityId) {
    BigDecimal remitAmount = em()
      .createQuery("SELECT SUM(amount) FROM TokenTransaction WHERE communityId = :communityId AND remitterId = :userId ", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId())
      .getSingleResult();
    if (remitAmount == null) remitAmount = BigDecimal.ZERO;
    
    BigDecimal benefitAmount = em()
      .createQuery("SELECT SUM(amount) FROM TokenTransaction WHERE communityId = :communityId AND beneficiaryId = :userId ", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId())
      .getSingleResult();
    if (benefitAmount == null) benefitAmount = BigDecimal.ZERO;
    
    return benefitAmount.subtract(remitAmount);
  }


  public BigDecimal pendingTransactionsAmount(Long userId, Long communityId) {
    BigDecimal amount = em()
      .createQuery("SELECT SUM(amount) FROM TokenTransaction " +
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
      .createQuery("SELECT COUNT(*) FROM TokenTransaction " +
          "WHERE blockchainCompletedAt IS NULL ", Long.class)
      .getSingleResult();
    return count != null ? count :  0L;
  }
}
