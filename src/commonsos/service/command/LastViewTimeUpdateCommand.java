package commonsos.service.command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @EqualsAndHashCode @Accessors(chain=true) @ToString
public class LastViewTimeUpdateCommand {
  private Long communityId;
}