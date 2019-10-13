package commonsos.command.batch;

import java.math.BigDecimal;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class CreateTokenTransactionForRedistributionCommand {
  private Community community;
  private User user;
  private BigDecimal rate;
}
