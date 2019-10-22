package commonsos.integration.wordpress.community;

import commonsos.integration.app.community.SearchCommunityNotificationTest;

public class SearchCommunityNotificationFromWPTest extends SearchCommunityNotificationTest {

  @Override
  protected String prefix() {
    return "/wordpress";
  }
}
