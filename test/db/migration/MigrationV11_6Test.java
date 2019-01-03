package db.migration;

import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

public class MigrationV11_6Test extends DBMigrationTest {

  @Ignore
  @Test
  public void migrate() throws SQLException {
    migrateTo("11.5");

    Operation operation =
        insertInto("users")
        .columns("admin", "username", "community_id")
        .values(false, "user1", 1)
        .values(false, "user2", 1)
        .values(false, "user3", 2)
        .values(false, "user4", 2)
        .build();
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), operation);
    dbSetup.launch();

    List<Map<String, String>> users = sql("SELECT * FROM users ORDER BY id");
    Long user1Id = Long.parseLong(users.get(0).get("id"));
    Long user2Id = Long.parseLong(users.get(0).get("id"));
    Long user3Id = Long.parseLong(users.get(0).get("id"));
    Long user4Id = Long.parseLong(users.get(0).get("id"));

    operation = sequenceOf(
            insertInto("transactions")
            .columns("remitter_id", "beneficiary_id")
            .values(user1Id, user2Id)
            .values(user2Id, user1Id)
            .values(user3Id, user4Id)
            .values(user4Id, user3Id)
            .build()
            );
    dbSetup = new DbSetup(new DataSourceDestination(dataSource), operation);
    dbSetup.launch();
    
    List<Map<String, String>> beforeMigration = sql(
        "SELECT t.id, t.remitter_id, u.community_id " +
        "FROM transactions t INNER JOIN users u ON u.id = t.remitter_id " +
        "ORDER BY t.id");

    migrateTo("11.6.3");
    List<Map<String, String>> afterMigration = sql(
        "SELECT id, remitter_id, community_id " +
        "FROM transactions " +
        "ORDER BY id");

    for (int i = 0; i < beforeMigration.size(); i++) {
      assertThat(beforeMigration.get(i).get("id")).isEqualTo(afterMigration.get(i).get("id"));
      assertThat(beforeMigration.get(i).get("remitter_id")).isEqualTo(afterMigration.get(i).get("remitter_id"));
      assertThat(beforeMigration.get(i).get("community_id")).isEqualTo(afterMigration.get(i).get("community_id"));
    }
  }
}
