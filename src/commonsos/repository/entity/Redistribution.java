package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;

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

@Entity @Table(name = "redistributions")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class Redistribution extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  @ManyToOne
  @JoinColumn(name = "community_id")
  private Community community;
  private boolean isAll;
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
  private BigDecimal rate;
  private boolean deleted;
}
