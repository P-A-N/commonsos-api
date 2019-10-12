package commonsos.repository.entity;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name="token_transactions")
@Getter @Setter @Accessors(chain=true) @ToString
public class TokenTransaction extends AbstractEntity {
  @Id @GeneratedValue(strategy=IDENTITY) private Long id;
  private Long communityId;
  private Long remitterUserId;
  private Long beneficiaryUserId;
  private BigDecimal fee;
  private boolean isFromAdmin;
  private boolean isFeeTransaction;
  private Long remitterAdminId;
  @Enumerated(value = STRING)
  private WalletType walletDivision;
  private boolean redistributed;
  private String description;
  private Long adId;
  private BigDecimal amount;
  private String blockchainTransactionHash;
  private Instant blockchainCompletedAt;
}
