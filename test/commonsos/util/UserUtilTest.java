package commonsos.util;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class UserUtilTest {

  @Test
  public void isAdmin() {
    // prepare
    Community community1 = new Community().setId(id("community1"));
    Community community2 = new Community().setId(id("community2"));
    User admin1 = new User().setId(id("admin1")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2)));
    User admin2 = new User().setId(id("admin2")).setCommunityUserList(asList(new CommunityUser().setCommunity(community2)));
    User user1 = new User().setId(id("user1")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(community1),
        new CommunityUser().setCommunity(community2)));
    User user2 = new User().setId(id("user2")).setCommunityUserList(asList());
    community1.setAdminUser(admin1);
    community2.setAdminUser(admin2);

    // execute & verify
    assertThat(UserUtil.isAdmin(admin1, community1.getId())).isTrue();
    assertThat(UserUtil.isAdmin(admin1, community2.getId())).isFalse();
    assertThat(UserUtil.isAdmin(admin2, community1.getId())).isFalse();
    assertThat(UserUtil.isAdmin(admin2, community2.getId())).isTrue();
    assertThat(UserUtil.isAdmin(user1, community1.getId())).isFalse();
    assertThat(UserUtil.isAdmin(user1, community2.getId())).isFalse();
    assertThat(UserUtil.isAdmin(user2, community1.getId())).isFalse();
    assertThat(UserUtil.isAdmin(user2, community2.getId())).isFalse();
  }
}