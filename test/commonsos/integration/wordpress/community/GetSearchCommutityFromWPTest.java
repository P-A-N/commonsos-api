package commonsos.integration.wordpress.community;

import commonsos.integration.app.community.SearchCommutityTest;

public class GetSearchCommutityFromWPTest extends SearchCommutityTest {

  @Override
  protected String prefix() {
    return "/wordpress";
  }
}
