package commonsos.service.command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class MessageThreadListCommand {
  private Long communityId;
  private String memberFilter;
  private String messageFilter;
}
