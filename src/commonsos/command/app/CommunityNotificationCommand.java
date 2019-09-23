package commonsos.command.app;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class CommunityNotificationCommand {
  private String updatedAt;
  private Long communityId;
  private String wordpressId;
  private Instant updatedAtInstant;
}
