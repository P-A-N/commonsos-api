package commonsos.util;

import static commonsos.TestId.id;
import static commonsos.repository.entity.AdType.GIVE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.User;

public class AdUtilTest {

  @Test
  public void isOwn() {
    Ad ad = new Ad().setCreatedBy(id("worker"));

    assertThat(AdUtil.isOwnAd(new User().setId(id("worker")), ad)).isTrue();
    assertThat(AdUtil.isOwnAd(new User().setId(id("stranger")), ad)).isFalse();
  }

  @Test
  public void isPayable() {
    User me = new User().setId(id("me"));
    User otherUser = new User().setId(id("other"));

    Ad buyAd = new Ad().setCreatedBy(id("other")).setType(AdType.WANT).setPoints(ONE);
    Ad sellAd = new Ad().setCreatedBy(id("other")).setType(GIVE).setPoints(ONE);
    Ad sellAdWithZeroPrice = new Ad().setCreatedBy(id("other")).setType(GIVE).setPoints(ZERO);

    assertThat(AdUtil.isPayableByUser(me, sellAd)).isTrue();
    assertThat(AdUtil.isPayableByUser(otherUser, buyAd)).isTrue();

    assertThat(AdUtil.isPayableByUser(me, sellAdWithZeroPrice)).isFalse();
    assertThat(AdUtil.isPayableByUser(me, buyAd)).isFalse();
    assertThat(AdUtil.isPayableByUser(otherUser, sellAd)).isFalse();
    assertThat(AdUtil.isPayableByUser(me, buyAd)).isFalse();
  }
}