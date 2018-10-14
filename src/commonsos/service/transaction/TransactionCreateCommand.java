package commonsos.service.transaction;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class TransactionCreateCommand {
  private Long communityId;
  private Long beneficiaryId;
  private String description;
  private BigDecimal amount;
  private Long adId;
}
