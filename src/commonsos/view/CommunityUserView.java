package commonsos.view;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityUserView {
  private Long id;
  private String name;
  private Long adminUserId;
  private String description;
  private String tokenSymbol;
  private String photoUrl;
  private String coverPhotoUrl;
  private Instant walletLastViewTime;
  private Instant adLastViewTime;
  private Instant notificationLastViewTime;
}
