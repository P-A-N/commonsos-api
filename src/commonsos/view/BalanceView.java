package commonsos.view;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class BalanceView {
  private Long communityId;
  private String tokenSymbol;
  private BigDecimal balance;
}