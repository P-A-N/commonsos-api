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

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;

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

  public ResultList<TokenTransaction> searchUserTran(User user, Long communityId, PaginationCommand pagination) {
    TypedQuery<TokenTransaction> query = em()
      .createQuery("FROM TokenTransaction WHERE communityId = :communityId " +
          "AND (beneficiaryUserId = :userId OR remitterUserId = :userId) " + 
          "ORDER BY id", TokenTransaction.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId());
    
    ResultList<TokenTransaction> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<TokenTransaction> searchCommunityTran(Long communityId, WalletType walletType, PaginationCommand pagination) {
    TypedQuery<TokenTransaction> query = em()
      .createQuery("FROM TokenTransaction WHERE communityId = :communityId " +
          "AND walletDivision = :walletType " + 
          "ORDER BY id", TokenTransaction.class)
      .setParameter("communityId", communityId)
      .setParameter("walletType", walletType);
    
    ResultList<TokenTransaction> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public List<TokenTransaction> searchUnredistributedFeeTransaction(Long communityId) {
    List<TokenTransaction> resultList = em()
      .createQuery("FROM TokenTransaction WHERE communityId = :communityId " +
          "AND isFeeTransaction is true " +
          "AND redistributed is false " +
          "ORDER BY id", TokenTransaction.class)
      .setParameter("communityId", communityId)
      .getResultList();
    return resultList;
  }

  public TokenTransaction update(TokenTransaction transaction) {
    checkLocked(transaction);
    return em().merge(transaction);
  }

  public Optional<TokenTransaction> findByBlockchainTransactionHash(String blockchainTransactionHash) {
    try {
      return of(em()
        .createQuery("From TokenTransaction WHERE blockchainTransactionHash = :hash", TokenTransaction.class)
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
      .setParameter("adId", ad.getId())
      .getResultList();
    
    return !resultList.isEmpty();
  }

  public BigDecimal getBalanceFromTransactions(User user, Long communityId) {
    BigDecimal remitAmount = em()
      .createQuery("SELECT SUM(amount) FROM TokenTransaction WHERE communityId = :communityId AND remitterUserId = :userId ", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId())
      .getSingleResult();
    if (remitAmount == null) remitAmount = BigDecimal.ZERO;
    
    BigDecimal benefitAmount = em()
      .createQuery("SELECT SUM(amount) FROM TokenTransaction WHERE communityId = :communityId AND beneficiaryUserId = :userId ", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", user.getId())
      .getSingleResult();
    if (benefitAmount == null) benefitAmount = BigDecimal.ZERO;
    
    return benefitAmount.subtract(remitAmount);
  }

  public BigDecimal getPendingTransactionsAmount(Long userId, Long communityId) {
    BigDecimal amount = em()
      .createQuery("SELECT SUM(amount) FROM TokenTransaction " +
          "WHERE communityId = :communityId " +
          "AND blockchainCompletedAt IS NULL " +
          "AND remitterUserId = :userId", BigDecimal.class)
      .setParameter("communityId", communityId)
      .setParameter("userId", userId)
      .getSingleResult();
    return amount != null ? amount :  ZERO;
  }
  
  public Long getPendingTransactionsCount() {
    Long count = em()
      .createQuery("SELECT COUNT(*) FROM TokenTransaction " +
          "WHERE blockchainCompletedAt IS NULL ", Long.class)
      .getSingleResult();
    return count != null ? count :  0L;
  }
}
