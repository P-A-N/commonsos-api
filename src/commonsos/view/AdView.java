package commonsos.view;

import java.math.BigDecimal;
import java.time.Instant;

import commonsos.repository.entity.AdType;
import commonsos.repository.entity.PublishStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdView extends CommonView {
  private Long id;
  private Long communityId;
  private CommunityView community;
  private PublishStatus publishStatus;
  private String status;
  private UserView createdBy;
  private String title;
  private String description;
  private BigDecimal points;
  private String location;
  private boolean own;
  private Instant createdAt;
  private String photoUrl;
  private AdType type;
}
