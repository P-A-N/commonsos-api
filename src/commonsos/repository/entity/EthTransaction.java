package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name="eth_transactions")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class EthTransaction extends AbstractEntity {
  @Id @GeneratedValue(strategy=IDENTITY) private Long id;
  private Long communityId;
  private String blockchainTransactionHash;
  private Long remitterAdminId;
  private BigDecimal amount;
  private String description;
  private Instant blockchainCompletedAt;
}
