package commonsos.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateUserEmailAddressTemporaryCommand {
  private Long userId;
  private String newEmailAddress;
}
