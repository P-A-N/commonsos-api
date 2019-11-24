package commonsos.command.admin;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateRedistributionCommand {
  private Long communityId;
  private Long redistributionId;
  private boolean isAll;
  private Long userId;
  private BigDecimal redistributionRate;
}
