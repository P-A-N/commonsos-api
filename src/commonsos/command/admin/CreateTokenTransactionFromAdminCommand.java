package commonsos.command.admin;

import java.math.BigDecimal;

import commonsos.repository.entity.WalletType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class CreateTokenTransactionFromAdminCommand {
  private Long communityId;
  private WalletType wallet;
  private Long beneficiaryUserId;
  private BigDecimal amount;
}
