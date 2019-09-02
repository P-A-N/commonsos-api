package commonsos.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;

import commonsos.repository.entity.EthTransaction;

@Singleton
public class EthTransactionRepository extends Repository {

  @Inject
  public EthTransactionRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<EthTransaction> findByBlockchainTransactionHash(String blockchainTransactionHash) {
    try {
      return of(em()
        .createQuery("From EthTransaction WHERE blockchainTransactionHash = :hash", EthTransaction.class)
        .setLockMode(lockMode())
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
    return em().merge(transaction);
  }
}
