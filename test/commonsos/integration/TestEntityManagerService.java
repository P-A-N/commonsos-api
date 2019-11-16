package commonsos.integration;

import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import static com.ninja_squad.dbsetup.Operations.sql;

import java.util.Map;

import javax.inject.Singleton;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import commonsos.Configuration;
import commonsos.repository.EntityManagerService;

@Singleton
public class TestEntityManagerService extends EntityManagerService {

  private static Flyway flyway = new Flyway();
  
  public static Operation DELETE_ALL = deleteAllFrom(
      "message_thread_parties",
      "users",
      "ads",
      "messages",
      "message_threads",
      "token_transactions",
      "eth_transactions",
      "communities",
      "community_users",
      "community_notifications",
      "redistributions",
      "eth_balance_history",
      "temporary_community_users",
      "temporary_users",
      "temporary_email_address",
      "password_reset_request",
      "admins",
//      "roles",
      "temporary_admins",
      "temporary_admin_email_address");
  
  public static Operation DROP_ALL = sql(
      "DROP TABLE IF EXISTS "
      + "flyway_schema_history, "
      + "users, "
      + "communities, "
      + "community_users, "
      + "community_notifications, "
      + "redistributions, "
      + "eth_balance_history, "
      + "ads, "
      + "messages, "
      + "message_threads, "
      + "message_thread_parties, "
      + "token_transactions, "
      + "eth_transactions, "
      + "temporary_users, "
      + "temporary_community_users, "
      + "temporary_email_address, "
      + "password_reset_request, "
      + "admins, "
      + "roles, "
      + "temporary_admins, "
      + "temporary_admin_email_address "
      + "CASCADE;");

  @Override
  protected Map<String, String> getConfig() {
    configuration = new Configuration();
    Map<String, String> config = super.getConfig();
    
    return config;
  }
  
  public void closeFactory() {
    entityManagerFactory.close();
  }
  
  public void clearDbAndMigrate() {
    flyway.setDataSource(dataSource());
    try {
      flyway.validate();
      flyway.migrate();
      DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource()), DELETE_ALL);
      dbSetup.launch();
      
    } catch (FlywayException e) {
      DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource()), DROP_ALL);
      dbSetup.launch();
      flyway.migrate();
      
    }
  }

  public void runInTransaction(Runnable code) {
    runInTransaction((Executable<Void>) () -> {
      code.run();
      return null;
    });
  }
}
