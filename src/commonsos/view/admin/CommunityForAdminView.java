package commonsos.view.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityForAdminView {
  private Long communityId;
  private String communityName;
  private String tokenName;
  private String tokenSymbol;
  private BigDecimal transactionFee;
  private String description;
  private String status;
  private String adminPageUrl;
  private BigDecimal totalSupply;
  private Integer totalMember;
  private String photoUrl;
  private String coverPhotoUrl;
  private Instant createdAt;
  private List<AdminView> adminList;
}
