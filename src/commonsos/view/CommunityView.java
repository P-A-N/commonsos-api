package commonsos.view;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityView extends CommonView {
  // id and communityId is the same thing
  private Long id;
  private Long communityId;
  // name and communityName is the same thing
  private String name;
  private String communityName;
  private BigDecimal transactionFee;
  private String description;
  private String status;
  private String adminPageUrl;
  private String photoUrl;
  private String coverPhotoUrl;
  private List<AdminView> adminList;
  private Long adminUserId;
  private Instant createdAt;
  private String tokenName;
  private String tokenSymbol;
  private BigDecimal totalSupply;
  private Integer totalMember;
  private BigDecimal ethBalance;
}
