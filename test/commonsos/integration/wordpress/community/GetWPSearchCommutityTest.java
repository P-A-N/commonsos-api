package commonsos.integration.wordpress.community;

import commonsos.integration.app.community.GetSearchCommutityTest;

public class GetWPSearchCommutityTest extends GetSearchCommutityTest {

  @Override
  protected String prefix() {
    return "/wordpress";
  }
}
