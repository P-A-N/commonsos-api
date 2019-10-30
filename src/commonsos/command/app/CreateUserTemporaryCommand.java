package commonsos.command.app;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class CreateUserTemporaryCommand {
  private String username;
  private String password;
  private String telNo;
  private String firstName;
  private String lastName;
  private String description;
  private String location;
  private List<Long> communityList;
  private String emailAddress;
}
