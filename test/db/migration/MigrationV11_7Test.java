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

public class MigrationV11_7Test extends DBMigrationTest {

  @Ignore
  @Test
  public void migrate() throws SQLException {
    migrateTo("11.6");

    Operation createCommunity = sequenceOf(
        insertInto("communities")
        .columns("name")
        .values("community1")
        .values("community2")
        .values("community3")
        .values("community4")
        .build()
        );
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), createCommunity);
    dbSetup.launch();

    List<Map<String, String>> communities = sql("SELECT * FROM communities ORDER BY id");
    Long community1Id = Long.parseLong(communities.get(0).get("id"));
    Long community2Id = Long.parseLong(communities.get(1).get("id"));
    Long community3Id = Long.parseLong(communities.get(2).get("id"));
    Long community4Id = Long.parseLong(communities.get(3).get("id"));

    Operation createUser = sequenceOf(
        insertInto("users")
        .columns("username", "community_id", "admin")
        .values("user1_1", community1Id, true)
        .values("user1_2", community1Id, false)
        .values("user1_3", community1Id, false)
        .values("user2_1", community2Id, true)
        .values("user3_1", community3Id, false)
        .build()
        );
    dbSetup = new DbSetup(new DataSourceDestination(dataSource), createUser);
    dbSetup.launch();

    List<Map<String, String>> users = sql("SELECT * FROM users ORDER BY id");
    Long user1_1Id = Long.parseLong(users.get(0).get("id"));
    Long user1_2Id = Long.parseLong(users.get(1).get("id"));
    Long user1_3Id = Long.parseLong(users.get(2).get("id"));
    Long user2_1Id = Long.parseLong(users.get(3).get("id"));
    Long user3_1Id = Long.parseLong(users.get(4).get("id"));
    
    migrateTo("11.7.3");
    communities = sql("SELECT * FROM communities ORDER BY id");

    assertThat(communities.size()).isEqualTo(4);
    assertThat(communities.get(0).get("id")).isEqualTo(community1Id.toString());
    assertThat(communities.get(0).get("admin_user_id")).isEqualTo(user1_1Id.toString());
    assertThat(communities.get(1).get("id")).isEqualTo(community2Id.toString());
    assertThat(communities.get(1).get("admin_user_id")).isEqualTo(user2_1Id.toString());
    assertThat(communities.get(2).get("id")).isEqualTo(community3Id.toString());
    assertThat(communities.get(2).get("admin_user_id")).isNull();
    assertThat(communities.get(3).get("id")).isEqualTo(community4Id.toString());
    assertThat(communities.get(3).get("admin_user_id")).isNull();
  }
}
