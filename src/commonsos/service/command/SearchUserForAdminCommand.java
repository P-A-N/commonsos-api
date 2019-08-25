package commonsos.service.command;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class SearchUserForAdminCommand {
  private String username;
  private String emailAddress;
  private Long communityId;
}