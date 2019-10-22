package commonsos.repository.entity;

import static javax.persistence.EnumType.STRING;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "communities")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class Community extends AbstractEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private String name;
  @Enumerated(value = STRING)
  private PublishStatus publishStatus;
  private String description;
  private String tokenContractAddress;
  @OneToOne
  @JoinColumn(name = "admin_user_id")
  private User adminUser;
  private String photoUrl;
  private String coverPhotoUrl;
  private String mainWallet;
  private String mainWalletAddress;
  private String feeWallet;
  private String feeWalletAddress;
  private BigDecimal fee;
  private String adminPageUrl;
  private boolean deleted;
}
