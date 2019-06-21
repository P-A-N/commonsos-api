package db.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.BeforeClass;

public class DBMigrationTest {

  protected static final Flyway flyway = new Flyway();
  protected static final JdbcDataSource dataSource = new JdbcDataSource();

  @BeforeClass
  public static void setup() {
    dataSource.setURL("jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    flyway.setDataSource(dataSource);
  }
  
  @Before
  public void cleanBefore() {
    flyway.clean();
  }
  
  public static void migrateTo(String version) {
    flyway.setTargetAsString(version);
    flyway.migrate();
  }
  
  public static List<Map<String, String>> sql(String sql) throws SQLException {
    List<Map<String, String>> results = new ArrayList<>();
    
    try (Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(sql);) {
      ResultSetMetaData meta = rs.getMetaData();
      while(rs.next()) {
        Map<String, String> row = new HashMap<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
          row.put(meta.getColumnName(i).toLowerCase(), rs.getString(i));
        }
        results.add(row);
      }
    }
    
    return results;
  }
}
