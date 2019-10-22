package commonsos.repository.entity;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;

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
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class Ad extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private Long createdUserId;
  @Enumerated(value = STRING)
  private PublishStatus publishStatus;
  private String status;
  @Enumerated(value = STRING) private AdType type;
  private String title;
  private String description;
  private BigDecimal points;
  private String location;
  private String photoUrl;
  private Long communityId;
  private boolean deleted;
}
