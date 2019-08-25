package commonsos.service.blockchain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TokenBalance {
  private Long communityId;
  private BigDecimal balance;
  private CommunityToken token;
}
