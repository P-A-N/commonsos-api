package commonsos.domain.ad;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name="ads")
@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class Ad {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private Long createdBy;
  @Enumerated(value = STRING) private AdType type;
  private String title;
  private String description;
  private BigDecimal points;
  private String location;
  private Instant createdAt;
  private String photoUrl;
  private Long communityId;
  private boolean deleted;
}
