package commonsos.controller.command.app;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class GroupMessageThreadUpdateCommand {
  private Long threadId;
  private String title;
  private List<Long> memberIds;
}
