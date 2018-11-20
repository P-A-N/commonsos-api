package commonsos.repository;

import static commonsos.integration.TestEntityManagerService.DELETE_ALL;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.hibernate.internal.SessionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;

import commonsos.ThreadValue;
import commonsos.integration.TestEntityManagerService;

public class RepositoryTest {
  protected static TestEntityManagerService emService = new TestEntityManagerService();

  protected EntityManager em() {
    return emService.get();
  }

  @BeforeClass
  public static void setupRepositoryTest() {
    emService.init();
    emService.clearDbAndMigrate();
  }
  
  @Before
  public void prepare() {
    ThreadValue.setReadOnly(false);
    
//    EntityTransaction tran = emService.get().getTransaction();
//    if (!tran.isActive()) tran.begin();
  }

  @After
  public void cleanup() throws Exception {
//    EntityTransaction tran = emService.get().getTransaction();
//    if (tran.isActive()) tran.commit();
    
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(emService.dataSource()), DELETE_ALL);
    dbSetup.launch();
  }

  protected static void executeSQL(String sql, EntityManagerFactory entityManagerFactory) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();
    Connection connection = ((SessionImpl) entityManager).connection();
    try {
      connection.createStatement().execute(sql);
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    transaction.commit();
  }

  protected static Path createTempFile() {
    try {
      return Files.createTempFile("", "");
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected <T> T inTransaction(EntityManagerService.Executable<T> code) {
    try {
      return emService.runInTransaction(code);
    }
    finally {
      emService.close();
    }
  }

  protected void inTransaction(Runnable code) {
    inTransaction(() -> {
      code.run();
      return null;
    });
  }
}


