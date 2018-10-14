package commonsos.integration;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Persistence;

import commonsos.EntityManagerService;

@Singleton
public class TestEntityManagerService extends EntityManagerService {

  @Inject
  @Override
  public void init() {
    Map<String, String> config = new HashMap<>();
    config.put("hibernate.connection.url", "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    this.entityManagerFactory = Persistence.createEntityManagerFactory("commonsos", config);
  }
}
