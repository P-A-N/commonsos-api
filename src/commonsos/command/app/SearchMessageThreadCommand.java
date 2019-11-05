package commonsos.command.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class SearchMessageThreadCommand {
  private Long communityId;
  private String memberFilter;
  private String messageFilter;
}