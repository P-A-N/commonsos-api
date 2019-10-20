package commonsos.command.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class AdminLoginCommand {
  private String emailAddress;
  private String password;
}
