package commonsos.controller.command.app;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class TransactionCreateCommand {
  private Long communityId;
  private Long beneficiaryId;
  private String description;
  private BigDecimal transactionFee;
  private BigDecimal amount;
  private Long adId;
}
