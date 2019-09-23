package commonsos.util;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.User;

public class AdUtilTest {

  @Test
  public void isOwn() {
    Ad ad = new Ad().setCreatedUserId(id("worker"));

    assertThat(AdUtil.isOwnAd(new User().setId(id("worker")), ad)).isTrue();
    assertThat(AdUtil.isOwnAd(new User().setId(id("stranger")), ad)).isFalse();
  }
}