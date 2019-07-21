package commonsos.service.command;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class CreateAccountTemporaryCommand {
  private String username;
  private String password;
  private String firstName;
  private String lastName;
  private String description;
  private String location;
  private List<Long> communityList;
  private String emailAddress;
}
