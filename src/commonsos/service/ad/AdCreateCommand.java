package commonsos.service.ad;

import java.math.BigDecimal;

import commonsos.repository.ad.AdType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdCreateCommand {
  private Long communityId;
  private String title;
  private String description;
  private BigDecimal points;
  private String location;
  private AdType type;
  private String photoUrl;
}
