package commonsos.view.app;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityUserView extends CommunityView {
  private BigDecimal balance;
  private Instant walletLastViewTime;
  private Instant adLastViewTime;
  private Instant notificationLastViewTime;
}
