package commonsos.service.blockchain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityToken {
  private String tokenName;
  private String tokenSymbol;
  private BigDecimal totalSupply;
}
