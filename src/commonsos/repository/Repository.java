package commonsos.repository;

import static javax.persistence.LockModeType.NONE;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import commonsos.ThreadValue;

public abstract class Repository {
  private final EntityManagerService entityManagerService;

  public Repository(EntityManagerService entityManagerService) {
    this.entityManagerService = entityManagerService;
  }

  protected EntityManager em() {
    return entityManagerService.get();
  }

  protected LockModeType lockMode() {
    return ThreadValue.isReadOnly() ? NONE : PESSIMISTIC_WRITE;
  }
}
