package commonsos.integration.wordpress.community;

import commonsos.integration.app.community.GetCommunityNotificationListTest;

public class GetWPCommunityNotificationListTest extends GetCommunityNotificationListTest {

  @Override
  protected String prefix() {
    return "/wordpress";
  }
}
