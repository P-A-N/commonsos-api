package commonsos.service.ad;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import commonsos.repository.ad.AdType;

@Getter @Setter @Accessors(chain=true)
public class AdCreateCommand {
  private String title;
  private String description;
  private BigDecimal amount;
  private String location;
  private AdType type;
  private String photoUrl;
}
