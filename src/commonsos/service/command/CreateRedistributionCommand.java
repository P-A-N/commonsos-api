package commonsos.service.command;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CreateRedistributionCommand {
  private Long communityId;
  private boolean isAll;
  private Long userId;
  private Double redistributionRate;
}
