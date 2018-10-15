package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public class V11_7_2__communities_adminUserId implements JdbcMigration {

  @Override
  public void migrate(Connection connection) throws Exception {
    List<AdminUser> adminUserList = new ArrayList<>();
    try (Statement stat = connection.createStatement();
        ResultSet rs = stat.executeQuery("SELECT id, community_id FROM users WHERE admin = true")) {
      while(rs.next()) {
        Long adminUserId = rs.getLong("id");
        Long communityId = rs.getLong("community_id");
        adminUserList.add(new AdminUser(adminUserId, communityId));
      }
    }

    String sql = "UPDATE communities SET admin_user_id = ? where id = ?";
    for (int i = 0; i < adminUserList.size(); i++) {
      try (PreparedStatement pstat = connection.prepareStatement(sql)) {
        pstat.setLong(1, adminUserList.get(i).adminUserId);
        pstat.setLong(2, adminUserList.get(i).communityId);
        int result = pstat.executeUpdate();
        if (result != 1) throw new RuntimeException();
      }
    }
  }

  private class AdminUser {
    public Long adminUserId;
    public Long communityId;
    public AdminUser(Long adminUserId, Long communityId) {
      this.adminUserId = adminUserId;
      this.communityId = communityId;
    }
  }
}
