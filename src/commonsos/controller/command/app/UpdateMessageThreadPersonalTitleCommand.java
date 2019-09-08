package commonsos.controller.command.app;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class UpdateMessageThreadPersonalTitleCommand {
  private Long threadId;
  private String personalTitle;
}
