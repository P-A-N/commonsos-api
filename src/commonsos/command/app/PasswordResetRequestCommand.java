package commonsos.command.app;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class PasswordResetRequestCommand {
  private String emailAddress;
}
