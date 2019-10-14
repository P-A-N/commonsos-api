package commonsos.service;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;

import commonsos.repository.EntityManagerService;

public abstract class AbstractService {
  @Inject private EntityManagerService entityManagerService;
  
  protected void commitAndStartNewTran() {
    EntityTransaction transaction = entityManagerService.get().getTransaction();
    if (transaction.isActive()) transaction.commit();
    transaction.begin();
  }
}
