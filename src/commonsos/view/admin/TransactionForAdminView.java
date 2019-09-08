package commonsos.view.admin;

import java.math.BigDecimal;
import java.time.Instant;

import commonsos.repository.entity.WalletType;
import commonsos.view.CommonView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TransactionForAdminView extends CommonView {
  private Long communityId;
  private WalletType wallet;
  private Boolean isFromAdmin;
  private UserForAdminView remitter;
  private UserForAdminView beneficiary;
  private BigDecimal amount;
  private Instant createdAt;
  private boolean completed;
  private boolean debit;
}
