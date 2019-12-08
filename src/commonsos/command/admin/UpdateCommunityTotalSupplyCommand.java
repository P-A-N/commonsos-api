package commonsos.command.admin;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateCommunityTotalSupplyCommand {
  private Long communityId;
  private BigDecimal totalSupply;
}
