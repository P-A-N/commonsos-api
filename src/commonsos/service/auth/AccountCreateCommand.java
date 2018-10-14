package commonsos.service.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class AccountCreateCommand {
  private String username;
  private String password;
  private String firstName;
  private String lastName;
  private String description;
  private String location;
  private Long communityId;
  private String emailAddress;
  private boolean waitUntilCompleted;
}
