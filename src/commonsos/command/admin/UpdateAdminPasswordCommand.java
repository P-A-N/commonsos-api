package commonsos.command.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateAdminPasswordCommand {
  private Long adminId;
  private String newPassword;
}
