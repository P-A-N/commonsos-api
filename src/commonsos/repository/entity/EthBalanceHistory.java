package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "eth_balance_history")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class EthBalanceHistory extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  @ManyToOne
  @JoinColumn(name = "community_id")
  private Community community;
  private LocalDate baseDate;
  private BigDecimal ethBalance;
}
