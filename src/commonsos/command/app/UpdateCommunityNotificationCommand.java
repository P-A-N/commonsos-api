package commonsos.command.app;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateCommunityNotificationCommand {
  private String updatedAt;
  private Long communityId;
  private String wordpressId;
  private Instant updatedAtInstant;
}
