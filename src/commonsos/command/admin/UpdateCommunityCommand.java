package commonsos.command.admin;

import java.math.BigDecimal;
import java.util.List;

import commonsos.repository.entity.CommunityStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateCommunityCommand {
  private Long communityId;
  private String communityName;
  private BigDecimal transactionFee;
  private String description;
  private CommunityStatus status;
  private List<Long> adminIdList;
}
