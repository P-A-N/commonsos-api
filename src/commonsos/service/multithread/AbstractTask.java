package commonsos.service.multithread;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;

import commonsos.repository.EntityManagerService;
import commonsos.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask implements Runnable {

  private static int MAX_REPEAT_COUNT = 3;
  @Inject EntityManagerService entityManagerService;

  @Override
  public void run() {
    
    for (int i = 0; i < MAX_REPEAT_COUNT; i++) {
      log.info(String.format("Task execution start. Task:%s", this));
      
      EntityTransaction transaction = entityManagerService.get().getTransaction();
      try {
        if (!transaction.isActive()) transaction.begin();
        
        runTask();
        
        if (transaction.isActive()) transaction.commit();
        
        break;
      } catch (Exception e) {
        log.error(String.format("Task execution failed. Task:%s [repeat_count=%d]", this, i), e);
        if (transaction.isActive()) transaction.rollback();
        ThreadUtil.sleep(1000);
      }
    }
    
    log.info(String.format("Task execution finish. Task:%s", this));
  }
  
  protected int getMaxRepeatCount() {
    return MAX_REPEAT_COUNT;
  }

  protected void commitAndStartNewTran() {
    EntityTransaction transaction = entityManagerService.get().getTransaction();
    if (transaction.isActive()) transaction.commit();
    transaction.begin();
  }
  
  public abstract void runTask();
}
