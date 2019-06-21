package commonsos.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.repository.EntityManagerService.Executable;

@ExtendWith(MockitoExtension.class)
public class EntityManagerServiceTest {
  @Mock EntityManagerFactory entityManagerFactory;
  @InjectMocks @Spy EntityManagerService entityManagerService = new EntityManagerService() {
    @Override public void init() { }
  };

  @Test
  public void get() throws Exception {
    EntityManager entityManager = mock(EntityManager.class);
    when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);

    assertThat(entityManagerService.get()).isEqualTo(entityManager);
    assertThat(entityManagerService.get()).isEqualTo(entityManager);

    verify(entityManagerFactory, times(1)).createEntityManager();
  }

  @Test
  public void close() throws Exception {
    EntityManager entityManager = mock(EntityManager.class);
    entityManagerService.em.set(entityManager);

    entityManagerService.close();

    verify(entityManager).close();
    assertThat(entityManagerService.em.get()).isNull();
  }

  @Test
  public void remove_nullSafe() throws Exception {
    EntityManager entityManager = mock(EntityManager.class);
    entityManagerService.em = spy(entityManagerService.em);

    entityManagerService.close();

    verify(entityManager, never()).close();
    verify(entityManagerService.em, never()).remove();
  }

  private static class MyThread extends Thread {
    public EntityManager entityManager;
    private EntityManagerService entityManagerService;

    public MyThread(EntityManagerService entityManagerService) {
      this.entityManagerService = entityManagerService;
    }

    @Override
    public void run() {
      entityManager = entityManagerService.get();
    }
  }

  @Test
  public void differentThreadsGetDifferentEntityManagers() throws Exception {
    EntityManager entityManager1 = mock(EntityManager.class);
    EntityManager entityManager2 = mock(EntityManager.class);
    when(entityManagerFactory.createEntityManager())
      .thenReturn(entityManager1)
      .thenReturn(entityManager2);

    MyThread thread1 = new MyThread(entityManagerService);
    thread1.start();
    thread1.join();

    MyThread thread2 = new MyThread(entityManagerService);
    thread2.start();
    thread2.join();

    verify(entityManagerFactory, times(2)).createEntityManager();
    assertThat(thread1.entityManager).isEqualTo(entityManager1);
    assertThat(thread2.entityManager).isEqualTo(entityManager2);
  }

  @Test
  public void transactional() throws Throwable {
    EntityManager entityManager = mock(EntityManager.class);
    entityManagerService.em.set(entityManager);
    EntityTransaction transaction = mock(EntityTransaction.class);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false).thenReturn(true);

    Executable<String> code = mock(Executable.class);
    when(code.execute()).thenReturn("test");

    assertThat(entityManagerService.runInTransaction(code)).isEqualTo("test");

    InOrder inOrder = inOrder(transaction, code);
    inOrder.verify(transaction).begin();
    inOrder.verify(code).execute();
    inOrder.verify(transaction).commit();
    verify(transaction, never()).rollback();
  }

  @Test
  public void transactional_rollbacksInCaseOfRuntimeException() throws Throwable {
    EntityManager entityManager = mock(EntityManager.class);
    entityManagerService.em.set(entityManager);
    EntityTransaction transaction = mock(EntityTransaction.class);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false).thenReturn(true);

    Executable<String> code = mock(Executable.class);
    when(code.execute()).thenThrow(new RuntimeException());

    assertThrows(RuntimeException.class, () -> entityManagerService.runInTransaction(code));
    InOrder inOrder = inOrder(transaction, code);
    inOrder.verify(transaction).begin();
    inOrder.verify(code).execute();
    inOrder.verify(transaction).rollback();
    verify(transaction, never()).commit();
  }

  @Test
  public void transactional_rollbacksInCaseOfCheckedException() throws Throwable {
    EntityManager entityManager = mock(EntityManager.class);
    entityManagerService.em.set(entityManager);
    EntityTransaction transaction = mock(EntityTransaction.class);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false).thenReturn(true);

    Executable<String> code = mock(Executable.class);
    when(code.execute()).thenThrow(new Exception());

    assertThrows(Exception.class, () -> entityManagerService.runInTransaction(code));
    InOrder inOrder = inOrder(transaction, code);
    inOrder.verify(transaction).begin();
    inOrder.verify(code).execute();
    inOrder.verify(transaction).rollback();
    verify(transaction, never()).commit();
  }

  @Test
  public void runInTransactionSupportsNestedCalls() throws Throwable {
    EntityManager entityManager = mock(EntityManager.class);
    entityManagerService.em.set(entityManager);
    EntityTransaction transaction = mock(EntityTransaction.class);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false).thenReturn(true);

    Executable<String> innerCode = mock(Executable.class);
    Executable<String> code = spy(new Executable<String>() {
      @Override
      public String execute() throws Throwable {
        return entityManagerService.runInTransaction(innerCode);
      }
    });

    entityManagerService.runInTransaction(code);

    InOrder inOrder = inOrder(transaction, innerCode, code);
    inOrder.verify(transaction).begin();
    inOrder.verify(code).execute();
    inOrder.verify(innerCode).execute();
    inOrder.verify(transaction).commit();
    verify(transaction, never()).rollback();
  }

  @Test
  public void defaultSchema() throws Exception {
    when(entityManagerFactory.getProperties()).thenReturn(Collections.singletonMap("hibernate.default_schema", "schema"));

    assertThat(entityManagerService.defaultSchema()).isEqualTo("schema");
  }
}
