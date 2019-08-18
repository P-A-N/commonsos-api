package commonsos.view.admin;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class RedistributionView {
  private Long redistributionId;
  private Boolean isAll;
  private Long userId;
  private String username;
  private BigDecimal redistributionRate;
}