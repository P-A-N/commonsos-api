package commonsos.view;

import java.math.BigDecimal;
import java.time.Instant;

import commonsos.repository.entity.WalletType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TokenTransactionView extends CommonView {
  private Long communityId;
  private WalletType wallet;
  private Boolean isFromAdmin;
  private AdminView remitterAdmin;
  private UserView remitter;
  private UserView beneficiary;
  private BigDecimal amount;
  private String description;
  private Instant createdAt;
  private boolean completed;
  private boolean debit;
}
