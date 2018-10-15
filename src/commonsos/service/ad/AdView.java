package commonsos.service.ad;

import java.math.BigDecimal;
import java.time.Instant;

import commonsos.repository.ad.AdType;
import commonsos.service.view.UserView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdView {
  private Long id;
  private Long communityId;
  private UserView createdBy;
  private String title;
  private String description;
  private BigDecimal points;
  private String location;
  private boolean own;
  private boolean payable;
  private Instant createdAt;
  private String photoUrl;
  private AdType type;
}
