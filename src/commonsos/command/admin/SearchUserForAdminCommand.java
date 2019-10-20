package commonsos.command.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class SearchUserForAdminCommand {
  private String username;
  private String emailAddress;
  private Long communityId;
}
