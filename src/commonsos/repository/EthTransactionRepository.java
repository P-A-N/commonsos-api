package commonsos.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;

import commonsos.exception.EthTransactionNotFoundException;
import commonsos.repository.entity.EthTransaction;

@Singleton
public class EthTransactionRepository extends Repository {

  @Inject
  public EthTransactionRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<EthTransaction> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM EthTransaction WHERE id = :id", EthTransaction.class)
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public EthTransaction findStrictById(Long id) {
    return findById(id).orElseThrow(EthTransactionNotFoundException::new);
  }

  public Optional<EthTransaction> findByBlockchainTransactionHash(String blockchainTransactionHash) {
    try {
      return of(em()
        .createQuery("From EthTransaction WHERE blockchainTransactionHash = :hash", EthTransaction.class)
        .setParameter("hash", blockchainTransactionHash)
        .getSingleResult());
    }
    catch (NoResultException e) {
        return empty();
    }
  }
  
  public EthTransaction create(EthTransaction transaction) {
    em().persist(transaction);
    return transaction;
  }

  public EthTransaction update(EthTransaction transaction) {
    checkLocked(transaction);
    return em().merge(transaction);
  }
}
