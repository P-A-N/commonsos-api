package commonsos.view.admin;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityUserForAdminView {
  private Long id;
  private String name;
  private String tokenSymbol;
  private BigDecimal balance;
}
