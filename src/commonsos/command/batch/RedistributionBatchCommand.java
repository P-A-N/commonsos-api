package commonsos.command.batch;

import java.util.List;
import java.util.Map;

import commonsos.repository.entity.Community;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class RedistributionBatchCommand {
  private Map<Community, List<CreateTokenTransactionForRedistributionCommand>> commandMap;
}
