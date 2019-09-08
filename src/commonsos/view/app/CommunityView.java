package commonsos.view.app;

import java.math.BigDecimal;

import commonsos.view.CommonView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityView extends CommonView {
  private Long id;
  private String name;
  private Long adminUserId;
  private String description;
  private String tokenSymbol;
  private String photoUrl;
  private String coverPhotoUrl;
  private BigDecimal transactionFee;
}
