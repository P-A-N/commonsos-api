package commonsos.service.multithread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

import commonsos.exception.ServerErrorException;

@Singleton
public class TaskExecutorService {

  @Inject Injector injector;

  public static final int MAXIMUM_POOL_SIZE = 10;
  ExecutorService executor;

  @Inject
  public void init() {
    executor = Executors.newFixedThreadPool(MAXIMUM_POOL_SIZE);
  }

  public void execute(AbstractTask task) {
    injector.injectMembers(task);
    executor.execute(task);
  }

  public void executeAndWait(AbstractTask task) {
    injector.injectMembers(task);
    Future<?> future = executor.submit(task);
    try {
      future.get();
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }
}
