package commonsos.view.app;

import java.time.Instant;

import commonsos.view.CommonView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class MessageView extends CommonView {
  private Long id;
  private Instant createdAt;
  private Long createdBy;
  private String text;
}
