package commonsos.command.admin;

import java.math.BigDecimal;
import java.util.List;

import commonsos.repository.entity.PublishStatus;
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
  private PublishStatus status;
  private List<Long> adminIdList;
}
