package commonsos.command.app;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateGroupMessageThreadCommand {
  private Long threadId;
  private String title;
  private List<Long> memberIds;
}
