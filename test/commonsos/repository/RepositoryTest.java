package commonsos.repository;

import static commonsos.integration.TestEntityManagerService.DELETE_ALL;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;

import commonsos.ThreadValue;
import commonsos.integration.TestEntityManagerService;

public class RepositoryTest {
  protected static TestEntityManagerService emService = new TestEntityManagerService();

  protected EntityManager em() {
    return emService.get();
  }

  @BeforeAll
  public static void setupRepositoryTest() {
    emService.init();
    emService.clearDbAndMigrate();
  }
  
  @BeforeEach
  public void prepare() {
    ThreadValue.setReadOnly(false);
    beginTran();
  }

  @AfterEach
  public void cleanup() throws Exception {
    commitTran();
    
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(emService.dataSource()), DELETE_ALL);
    dbSetup.launch();
  }
  
  @AfterAll
  public static void stopIntegrationTest() {
    emService.closeFactory();
  }

  protected <T> T inTransaction(EntityManagerService.Executable<T> code) {
    T result = emService.runInTransaction(code);
    beginTran();
    return result;
  }

  protected void inTransaction(Runnable code) {
    emService.runInTransaction(code);
    beginTran();
  }
  
  private void beginTran() {
    emService.close();
    EntityTransaction tran = emService.get().getTransaction();
    if (!tran.isActive()) tran.begin();
  }
  
  private void commitTran() {
    EntityTransaction tran = emService.get().getTransaction();
    if (tran.isActive()) tran.commit();
    emService.close();
  }
}


