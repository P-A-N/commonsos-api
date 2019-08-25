package commonsos.view;

import java.math.BigDecimal;

import commonsos.repository.entity.WalletType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityTokenBalanceView {
  private Long communityId;
  private WalletType wallet;
  private String tokenSymbol;
  private BigDecimal balance;
}