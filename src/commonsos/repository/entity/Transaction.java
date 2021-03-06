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
import lombok.experimental.Accessors;

@Entity @Table(name="transactions")
@Getter @Setter @Accessors(chain=true)
public class Transaction {
  @Id @GeneratedValue(strategy=IDENTITY) private Long id;
  private Long communityId;
  private Long remitterId;
  private Long beneficiaryId;
  private String description;
  private Long adId;
  private BigDecimal amount;
  private Instant createdAt;
  private String blockchainTransactionHash;
  private Instant blockchainCompletedAt;
}
