package commonsos.view.admin;

import java.math.BigDecimal;

import commonsos.view.CommonView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityUserForAdminView extends CommonView {
  private Long id;
  private String name;
  private String tokenSymbol;
  private BigDecimal balance;
}
