package commonsos.service.command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class UpdateNotificationCommand {
  private Long id;
  private String title;
  private String url;
}
