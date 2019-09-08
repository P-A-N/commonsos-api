package commonsos.controller.command.app;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class PasswordResetCommand {
  private String newPassword;
}
