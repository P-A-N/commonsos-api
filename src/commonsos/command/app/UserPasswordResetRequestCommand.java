package commonsos.command.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UserPasswordResetRequestCommand {
  private String currentPassword;
}