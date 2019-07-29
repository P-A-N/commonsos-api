package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Entity;
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
  private Long remitterId;
  private Long beneficiaryId;
  private String description;
  private Long adId;
  private BigDecimal amount;
  private String blockchainTransactionHash;
  private Instant blockchainCompletedAt;
}
