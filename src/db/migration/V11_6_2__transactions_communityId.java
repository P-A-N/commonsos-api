package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public class V11_6_2__transactions_communityId implements JdbcMigration {

  @Override
  public void migrate(Connection connection) throws Exception {
    List<TransactionWithCommunityId> list1 = new ArrayList<>();
    try (Statement stat = connection.createStatement();
        ResultSet rs = stat.executeQuery(
            "SELECT t.id, t.remitter_id, u.community_id " +
            "FROM transactions t INNER JOIN users u ON u.id = t.remitter_id " +
            "ORDER BY t.id")) {
      while(rs.next()) {
        Long transactionId = rs.getLong("id");
        Long remitterId = rs.getLong("remitter_id");
        Long communityId = rs.getLong("community_id");
        list1.add(new TransactionWithCommunityId(transactionId, remitterId, communityId));
      }
    }

    String sql = "UPDATE transactions SET community_id = ? where id = ?";
    for (int i = 0; i < list1.size(); i++) {
      try (PreparedStatement pstat = connection.prepareStatement(sql)) {
        pstat.setLong(1, list1.get(i).communityId);
        pstat.setLong(2, list1.get(i).transactionId);
        int result = pstat.executeUpdate();
        if (result != 1) throw new RuntimeException();
      }
    }
  }

  private class TransactionWithCommunityId {
    public Long transactionId;
    public Long remitterId;
    public Long communityId;
    public TransactionWithCommunityId(Long transactionId, Long remitterId, Long communityId) {
      this.transactionId = transactionId;
      this.remitterId = remitterId;
      this.communityId = communityId;
    }
  }
}
