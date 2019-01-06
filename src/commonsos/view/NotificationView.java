package commonsos.view;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class NotificationView {
  private Long id;
  private String title;
  private String url;
  private Instant createdAt;
}
